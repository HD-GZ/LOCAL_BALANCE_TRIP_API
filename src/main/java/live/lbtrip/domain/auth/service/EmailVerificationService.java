package live.lbtrip.domain.auth.service;

import java.time.Duration;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import live.lbtrip.domain.auth.dto.request.EmailVerificationConfirmRequest;
import live.lbtrip.domain.auth.dto.request.EmailVerificationResendRequest;
import live.lbtrip.domain.auth.dto.response.EmailVerificationResponse;
import live.lbtrip.domain.auth.model.SignupVerificationToken;
import live.lbtrip.domain.auth.repository.SignupVerificationTokenRepository;
import live.lbtrip.domain.user.model.User;
import live.lbtrip.domain.user.service.UserFinder;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import live.lbtrip.global.util.StringNormalizer;

@Service
@Transactional(readOnly = true)
public class EmailVerificationService {

    private final SignupVerificationTokenRepository tokenRepository;
    private final UserFinder userFinder;
    private final EmailService emailService;
    private final EmailVerificationCodeGenerator codeGenerator;
    private final Duration tokenExpiration;

    public EmailVerificationService(
        SignupVerificationTokenRepository tokenRepository,
        UserFinder userFinder,
        EmailService emailService,
        EmailVerificationCodeGenerator codeGenerator,
        @Value("${app.email-verification.code-expiration}") Duration tokenExpiration
    ) {
        this.tokenRepository = tokenRepository;
        this.userFinder = userFinder;
        this.emailService = emailService;
        this.codeGenerator = codeGenerator;
        this.tokenExpiration = tokenExpiration;
    }

    @Transactional
    public long issue(User user) {
        SignupVerificationToken token = SignupVerificationToken.create(
            user,
            codeGenerator.generate(),
            LocalDateTime.now().plus(tokenExpiration)
        );
        tokenRepository.save(token);
        emailService.sendVerificationEmail(user.getEmail(), token.getCode());
        return tokenExpiration.toSeconds();
    }

    @Transactional
    public EmailVerificationResponse confirm(EmailVerificationConfirmRequest request) {
        SignupVerificationToken token = tokenRepository.findByCode(StringNormalizer.trim(request.code()))
            .orElseThrow(() -> BusinessException.of(ErrorCode.EMAIL_VERIFICATION_CODE_NOT_FOUND));

        token.use(LocalDateTime.now());

        User user = token.getUser();
        user.verifyEmail();

        return EmailVerificationResponse.from(user);
    }

    @Transactional
    public EmailVerificationResponse resend(EmailVerificationResendRequest request) {
        String email = StringNormalizer.trimToLowerCase(request.email());
        User user = userFinder.findByEmail(email);
        user.validateEmailVerificationPending();

        issue(user);
        return EmailVerificationResponse.from(user);
    }

}
