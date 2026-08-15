package live.lbtrip.domain.user.service;

import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import live.lbtrip.domain.auth.service.RefreshTokenService;
import live.lbtrip.domain.user.dto.request.UserUpdateRequest;
import live.lbtrip.domain.user.dto.response.EmailAvailabilityResponse;
import live.lbtrip.domain.user.dto.response.UserResponse;
import live.lbtrip.domain.user.model.User;
import live.lbtrip.domain.user.repository.UserRepository;
import live.lbtrip.global.util.StringNormalizer;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final UserFinder userFinder;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;

    public EmailAvailabilityResponse checkEmailAvailability(String email) {
        String normalizedEmail = StringNormalizer.trimToLowerCase(email);
        return EmailAvailabilityResponse.of(!userRepository.existsByEmail(normalizedEmail));
    }

    public UserResponse getUser(Long userId) {
        User user = userFinder.findById(userId);
        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse updateUser(Long userId, UserUpdateRequest request) {
        User user = userFinder.findById(userId);

        user.update(StringNormalizer.trim(request.name()), request.birthDate(), request.gender());
        if (request.password() != null) {
            user.changePassword(passwordEncoder.encode(request.password()));
        }

        return UserResponse.from(user);
    }

    @Transactional
    public void withdraw(Long userId) {
        User user = userFinder.findById(userId);

        user.withdraw(LocalDateTime.now());
        refreshTokenService.deleteByUserId(userId);
    }
}
