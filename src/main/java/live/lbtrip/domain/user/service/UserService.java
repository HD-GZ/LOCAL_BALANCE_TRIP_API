package live.lbtrip.domain.user.service;

import java.util.Locale;

import live.lbtrip.domain.user.dto.response.EmailAvailabilityResponse;
import live.lbtrip.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;

	@Transactional(readOnly = true)
	public EmailAvailabilityResponse checkEmailAvailability(String email) {
		String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
		return new EmailAvailabilityResponse(!userRepository.existsByEmail(normalizedEmail));
	}
}
