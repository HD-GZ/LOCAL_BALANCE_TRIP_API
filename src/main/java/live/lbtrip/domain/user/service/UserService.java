package live.lbtrip.domain.user.service;

import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import live.lbtrip.domain.user.dto.response.EmailAvailabilityResponse;
import live.lbtrip.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public EmailAvailabilityResponse checkEmailAvailability(String email) {
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        return EmailAvailabilityResponse.of(!userRepository.existsByEmail(normalizedEmail));
    }
}
