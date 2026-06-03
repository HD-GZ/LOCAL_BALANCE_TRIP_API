package live.lbtrip.domain.user;

import java.util.Locale;

import live.lbtrip.domain.user.dto.EmailAvailabilityResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

	private final UserRepository userRepository;

	public UserService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Transactional(readOnly = true)
	public EmailAvailabilityResponse checkEmailAvailability(String email) {
		String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
		return new EmailAvailabilityResponse(!userRepository.existsByEmail(normalizedEmail));
	}
}
