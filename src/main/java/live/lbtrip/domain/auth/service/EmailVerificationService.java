package live.lbtrip.domain.auth.service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import live.lbtrip.domain.auth.dto.request.EmailVerificationConfirmRequest;
import live.lbtrip.domain.auth.dto.request.EmailVerificationResendRequest;
import live.lbtrip.domain.auth.dto.response.EmailVerificationResponse;
import live.lbtrip.domain.auth.model.EmailVerificationToken;
import live.lbtrip.domain.auth.repository.EmailVerificationTokenRepository;
import live.lbtrip.domain.user.model.User;
import live.lbtrip.domain.user.repository.UserRepository;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import live.lbtrip.global.util.StringNormalizer;

@Service
public class EmailVerificationService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String VERIFICATION_CODE_FORMAT = "%06d";
    private static final int VERIFICATION_CODE_BOUND = 1_000_000;

    private final EmailVerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final Duration tokenExpiration;

    public EmailVerificationService(
        EmailVerificationTokenRepository tokenRepository,
        UserRepository userRepository,
        EmailService emailService,
        @Value("${app.email-verification.code-expiration}") Duration tokenExpiration
    ) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.tokenExpiration = tokenExpiration;
    }

    @Transactional
    public long issue(User user) {
        EmailVerificationToken token = EmailVerificationToken.create(
            user,
            generateCode(),
            LocalDateTime.now().plus(tokenExpiration)
        );
        tokenRepository.save(token);
        emailService.sendVerificationEmail(user.getEmail(), token.getCode());
        return tokenExpiration.toSeconds();
    }

    @Transactional
    public EmailVerificationResponse confirm(EmailVerificationConfirmRequest request) {
        EmailVerificationToken token = tokenRepository.findByCode(StringNormalizer.trim(request.code()))
            .orElseThrow(() -> BusinessException.of(ErrorCode.EMAIL_VERIFICATION_CODE_NOT_FOUND));

        token.use(LocalDateTime.now());

        User user = token.getUser();
        user.verifyEmail();

        return EmailVerificationResponse.from(user);
    }

    @Transactional
    public EmailVerificationResponse resend(EmailVerificationResendRequest request) {
        String email = StringNormalizer.trimToLowerCase(request.email());
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> BusinessException.of(ErrorCode.USER_NOT_FOUND));

        if (user.isActive()) {
            throw BusinessException.of(ErrorCode.EMAIL_ALREADY_VERIFIED);
        }

        issue(user);
        return EmailVerificationResponse.from(user);
    }

    private String generateCode() {
        String code;
        do {
            code = VERIFICATION_CODE_FORMAT.formatted(RANDOM.nextInt(VERIFICATION_CODE_BOUND));
        } while (tokenRepository.existsByCode(code));
        return code;
    }
}
