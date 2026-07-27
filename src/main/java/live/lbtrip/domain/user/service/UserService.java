package live.lbtrip.domain.user.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import live.lbtrip.domain.user.dto.request.UserUpdateRequest;
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
    private final PasswordEncoder passwordEncoder;

    public EmailAvailabilityResponse checkEmailAvailability(String email) {
        String normalizedEmail = StringNormalizer.trimToLowerCase(email);
        return EmailAvailabilityResponse.of(!userRepository.existsByEmail(normalizedEmail));
    }

    public UserResponse getUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> BusinessException.of(ErrorCode.USER_NOT_FOUND));
        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse updateUser(Long userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> BusinessException.of(ErrorCode.USER_NOT_FOUND));

        if (request.name() != null) {
            user.changeName(StringNormalizer.trim(request.name()));
        }
        if (request.birthDate() != null) {
            user.changeBirthDate(request.birthDate());
        }
        if (request.gender() != null) {
            user.changeGender(request.gender());
        }
        if (request.newPassword() != null) {
            changePassword(user, request.currentPassword(), request.newPassword());
        }

        return UserResponse.from(user);
    }

    private void changePassword(User user, String currentPassword, String newPassword) {
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw BusinessException.of(ErrorCode.CURRENT_PASSWORD_MISMATCH);
        }
        user.changePassword(passwordEncoder.encode(newPassword));
    }
}
