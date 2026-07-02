package live.lbtrip.domain.user.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import live.lbtrip.domain.user.dto.response.EmailAvailabilityResponse;
import live.lbtrip.domain.user.dto.response.UserResponse;
import live.lbtrip.domain.user.model.User;
import live.lbtrip.domain.user.repository.UserRepository;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import live.lbtrip.global.util.StringNormalizer;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    public EmailAvailabilityResponse checkEmailAvailability(String email) {
        String normalizedEmail = StringNormalizer.trimToLowerCase(email);
        return EmailAvailabilityResponse.of(!userRepository.existsByEmail(normalizedEmail));
    }

    public UserResponse getUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> BusinessException.of(ErrorCode.USER_NOT_FOUND));
        return UserResponse.from(user);
    }
}
