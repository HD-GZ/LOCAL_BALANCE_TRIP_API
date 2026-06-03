package live.lbtrip.domain.auth.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Locale;
import java.security.SecureRandom;

import live.lbtrip.domain.auth.dto.EmailVerificationConfirmRequest;
import live.lbtrip.domain.auth.dto.EmailVerificationResendRequest;
import live.lbtrip.domain.auth.dto.EmailVerificationResponse;
import live.lbtrip.domain.auth.model.EmailVerificationToken;
import live.lbtrip.domain.auth.repository.EmailVerificationTokenRepository;
import live.lbtrip.domain.user.model.User;
import live.lbtrip.domain.user.model.UserStatus;
import live.lbtrip.domain.user.repository.UserRepository;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmailVerificationService {

	private static final SecureRandom RANDOM = new SecureRandom();

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
	public void issue(User user) {
		EmailVerificationToken token = createToken(user);
		tokenRepository.save(token);
		emailService.sendVerificationEmail(user, token.getCode());
	}

	@Transactional
	public EmailVerificationResponse confirm(EmailVerificationConfirmRequest request) {
		EmailVerificationToken token = tokenRepository.findByCode(request.code().trim())
			.orElseThrow(() -> BusinessException.of(ErrorCode.EMAIL_VERIFICATION_CODE_NOT_FOUND));

		if (token.isUsed()) {
			throw BusinessException.of(ErrorCode.EMAIL_VERIFICATION_CODE_USED);
		}
		if (token.isExpired(LocalDateTime.now())) {
			throw BusinessException.of(ErrorCode.EMAIL_VERIFICATION_CODE_EXPIRED);
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
			generateCode(),
			LocalDateTime.now().plus(tokenExpiration)
		);
	}

	private String generateCode() {
		String code;
		do {
			code = "%06d".formatted(RANDOM.nextInt(1_000_000));
		} while (tokenRepository.existsByCode(code));
		return code;
	}
}
