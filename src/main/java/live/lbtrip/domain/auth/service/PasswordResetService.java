package live.lbtrip.domain.auth.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import live.lbtrip.domain.auth.dto.request.PasswordResetCodeRequest;
import live.lbtrip.domain.auth.dto.request.PasswordResetConfirmRequest;
import live.lbtrip.domain.auth.dto.request.PasswordResetRequest;
import live.lbtrip.domain.auth.dto.response.PasswordResetCodeResponse;
import live.lbtrip.domain.auth.dto.response.PasswordResetTokenResponse;
import live.lbtrip.domain.auth.model.PasswordResetToken;
import live.lbtrip.domain.auth.repository.PasswordResetTokenRepository;
import live.lbtrip.domain.user.model.User;
import live.lbtrip.domain.user.repository.UserRepository;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import live.lbtrip.global.util.StringNormalizer;

@Service
@Transactional(readOnly = true)
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordResetCodeGenerator codeGenerator;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final Duration codeExpiration;
    private final Duration tokenExpiration;

    public PasswordResetService(
        UserRepository userRepository,
        PasswordResetTokenRepository tokenRepository,
        PasswordResetCodeGenerator codeGenerator,
        EmailService emailService,
        PasswordEncoder passwordEncoder,
        RefreshTokenService refreshTokenService,
        @Value("${app.password-reset.code-expiration}") Duration codeExpiration,
        @Value("${app.password-reset.token-expiration}") Duration tokenExpiration
    ) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.codeGenerator = codeGenerator;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenService = refreshTokenService;
        this.codeExpiration = codeExpiration;
        this.tokenExpiration = tokenExpiration;
    }

    @Transactional
    public PasswordResetCodeResponse request(PasswordResetCodeRequest request) {
        User user = findResettableUser(request.email());

        PasswordResetToken token = PasswordResetToken.create(
            user,
            codeGenerator.generate(),
            LocalDateTime.now().plus(codeExpiration)
        );
        tokenRepository.save(token);
        emailService.sendPasswordResetEmail(user.getEmail(), token.getCode());

        return PasswordResetCodeResponse.of(codeExpiration.toSeconds());
    }

    @Transactional
    public PasswordResetTokenResponse confirm(PasswordResetConfirmRequest request) {
        User user = findResettableUser(request.email());

        PasswordResetToken token = tokenRepository
            .findFirstByUserIdAndCodeOrderByIdDesc(user.getId(), StringNormalizer.trim(request.code()))
            .orElseThrow(() -> BusinessException.of(ErrorCode.PASSWORD_RESET_CODE_NOT_FOUND));

        LocalDateTime now = LocalDateTime.now();
        token.issueResetToken(UUID.randomUUID().toString(), now, now.plus(tokenExpiration));

        return PasswordResetTokenResponse.of(token.getResetToken(), tokenExpiration.toSeconds());
    }

    @Transactional
    public void reset(PasswordResetRequest request) {
        PasswordResetToken token = tokenRepository.findByResetToken(StringNormalizer.trim(request.resetToken()))
            .orElseThrow(() -> BusinessException.of(ErrorCode.PASSWORD_RESET_TOKEN_NOT_FOUND));

        User user = token.getUser();
        if (user.isWithdrawn()) {
            throw BusinessException.of(ErrorCode.USER_WITHDRAWN);
        }

        token.use(LocalDateTime.now());

        user.changePassword(passwordEncoder.encode(request.newPassword()));
        refreshTokenService.deleteByUserId(user.getId());
    }

    private User findResettableUser(String email) {
        User user = userRepository.findByEmail(StringNormalizer.trimToLowerCase(email))
            .orElseThrow(() -> BusinessException.of(ErrorCode.USER_NOT_FOUND));

        if (user.isWithdrawn()) {
            throw BusinessException.of(ErrorCode.USER_WITHDRAWN);
        }
        if (!user.isActive()) {
            throw BusinessException.of(ErrorCode.EMAIL_NOT_VERIFIED);
        }
        return user;
    }
}
