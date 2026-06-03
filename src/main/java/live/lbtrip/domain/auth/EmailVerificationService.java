package live.lbtrip.domain.auth;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

import live.lbtrip.domain.auth.dto.EmailVerificationConfirmRequest;
import live.lbtrip.domain.auth.dto.EmailVerificationResendRequest;
import live.lbtrip.domain.auth.dto.EmailVerificationResponse;
import live.lbtrip.domain.user.User;
import live.lbtrip.domain.user.UserRepository;
import live.lbtrip.domain.user.UserStatus;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmailVerificationService {

	private final EmailVerificationTokenRepository tokenRepository;
	private final UserRepository userRepository;
	private final EmailService emailService;
	private final Duration tokenExpiration;

	public EmailVerificationService(
		EmailVerificationTokenRepository tokenRepository,
		UserRepository userRepository,
		EmailService emailService,
		@Value("${app.email-verification.token-expiration}") Duration tokenExpiration
	) {
		this.tokenRepository = tokenRepository;
		this.userRepository = userRepository;
		this.emailService = emailService;
		this.tokenExpiration = tokenExpiration;
	}

	@Transactional
	public void issue(User user) {
		EmailVerificationToken token = createToken(user);
		tokenRepository.save(token);
		emailService.sendVerificationEmail(user, token.getToken());
	}

	@Transactional
	public EmailVerificationResponse confirm(EmailVerificationConfirmRequest request) {
		EmailVerificationToken token = tokenRepository.findByToken(request.token().trim())
			.orElseThrow(() -> BusinessException.of(ErrorCode.EMAIL_VERIFICATION_TOKEN_NOT_FOUND));

		if (token.isUsed()) {
			throw BusinessException.of(ErrorCode.EMAIL_VERIFICATION_TOKEN_USED);
		}
		if (token.isExpired(LocalDateTime.now())) {
			throw BusinessException.of(ErrorCode.EMAIL_VERIFICATION_TOKEN_EXPIRED);
		}

		User user = token.getUser();
		user.verifyEmail();
		token.use();

		return EmailVerificationResponse.from(user);
	}

	@Transactional
	public EmailVerificationResponse resend(EmailVerificationResendRequest request) {
		String email = request.email().trim().toLowerCase(Locale.ROOT);
		User user = userRepository.findByEmail(email)
			.orElseThrow(() -> BusinessException.of(ErrorCode.USER_NOT_FOUND));

		if (user.getStatus() == UserStatus.ACTIVE) {
			throw BusinessException.of(ErrorCode.EMAIL_ALREADY_VERIFIED);
		}

		issue(user);
		return EmailVerificationResponse.from(user);
	}

	private EmailVerificationToken createToken(User user) {
		return EmailVerificationToken.create(
			user,
			UUID.randomUUID().toString(),
			LocalDateTime.now().plus(tokenExpiration)
		);
	}
}
