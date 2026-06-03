package live.lbtrip.domain.auth;

import java.util.Locale;

import live.lbtrip.domain.auth.dto.SignupRequest;
import live.lbtrip.domain.auth.dto.SignupResponse;
import live.lbtrip.domain.user.User;
import live.lbtrip.domain.user.UserRepository;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final EmailVerificationService emailVerificationService;

	public AuthService(
		UserRepository userRepository,
		PasswordEncoder passwordEncoder,
		EmailVerificationService emailVerificationService
	) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.emailVerificationService = emailVerificationService;
	}

	@Transactional
	public SignupResponse signup(SignupRequest request) {
		validateSignupRequest(request);

		String email = normalizeEmail(request.email());
		if (userRepository.existsByEmail(email)) {
			throw BusinessException.of(ErrorCode.DUPLICATE_EMAIL);
		}

		User user = User.create(
			request.name().trim(),
			email,
			passwordEncoder.encode(request.password()),
			request.phoneNumber().trim(),
			request.age(),
			request.gender(),
			request.termsAgreed(),
			request.privacyAgreed(),
			request.marketingAgreed()
		);

		User savedUser = userRepository.save(user);
		emailVerificationService.issue(savedUser);
		return SignupResponse.from(savedUser);
	}

	private void validateSignupRequest(SignupRequest request) {
		if (!request.password().equals(request.passwordConfirm())) {
			throw BusinessException.of(ErrorCode.PASSWORD_CONFIRM_MISMATCH);
		}
		if (!request.termsAgreed() || !request.privacyAgreed()) {
			throw BusinessException.of(ErrorCode.REQUIRED_AGREEMENT_NOT_ACCEPTED);
		}
	}

	private String normalizeEmail(String email) {
		return email.trim().toLowerCase(Locale.ROOT);
	}
}
