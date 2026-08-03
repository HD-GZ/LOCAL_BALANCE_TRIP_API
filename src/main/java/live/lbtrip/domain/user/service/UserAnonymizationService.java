package live.lbtrip.domain.user.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import live.lbtrip.domain.auth.repository.PasswordResetTokenRepository;
import live.lbtrip.domain.auth.repository.RefreshTokenRepository;
import live.lbtrip.domain.auth.repository.SignupVerificationTokenRepository;
import live.lbtrip.domain.image.model.entity.Image;
import live.lbtrip.domain.image.repository.ImageRepository;
import live.lbtrip.domain.propensity.repository.PropensityRepository;
import live.lbtrip.domain.recommendation.repository.GeneratedCourseRepository;
import live.lbtrip.domain.recommendation.repository.RecommendedRegionRepository;
import live.lbtrip.domain.savedcourse.course.repository.SavedCourseRepository;
import live.lbtrip.domain.savedcourse.receipt.repository.TourReceiptRepository;
import live.lbtrip.domain.user.model.User;
import live.lbtrip.domain.user.model.UserStatus;
import live.lbtrip.domain.user.repository.UserRepository;
import live.lbtrip.global.storage.service.ImageStorage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = true)
public class UserAnonymizationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final SignupVerificationTokenRepository signupVerificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PropensityRepository propensityRepository;
    private final RecommendedRegionRepository recommendedRegionRepository;
    private final GeneratedCourseRepository generatedCourseRepository;
    private final SavedCourseRepository savedCourseRepository;
    private final TourReceiptRepository tourReceiptRepository;
    private final ImageRepository imageRepository;
    private final ImageStorage imageStorage;
    private final Duration gracePeriod;

    public UserAnonymizationService(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder,
        RefreshTokenRepository refreshTokenRepository,
        SignupVerificationTokenRepository signupVerificationTokenRepository,
        PasswordResetTokenRepository passwordResetTokenRepository,
        PropensityRepository propensityRepository,
        RecommendedRegionRepository recommendedRegionRepository,
        GeneratedCourseRepository generatedCourseRepository,
        SavedCourseRepository savedCourseRepository,
        TourReceiptRepository tourReceiptRepository,
        ImageRepository imageRepository,
        ImageStorage imageStorage,
        @Value("${app.withdrawal.grace-period}") Duration gracePeriod
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenRepository = refreshTokenRepository;
        this.signupVerificationTokenRepository = signupVerificationTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.propensityRepository = propensityRepository;
        this.recommendedRegionRepository = recommendedRegionRepository;
        this.generatedCourseRepository = generatedCourseRepository;
        this.savedCourseRepository = savedCourseRepository;
        this.tourReceiptRepository = tourReceiptRepository;
        this.imageRepository = imageRepository;
        this.imageStorage = imageStorage;
        this.gracePeriod = gracePeriod;
    }

    @Transactional
    public int anonymizeExpiredUsers() {
        LocalDateTime now = LocalDateTime.now();
        List<User> targets = userRepository.findAllByStatusAndDeletedAtIsNullAndWithdrawnAtBefore(
            UserStatus.WITHDRAWN, now.minus(gracePeriod));

        for (User user : targets) {
            purgeUserData(user);
            user.anonymize(passwordEncoder.encode(UUID.randomUUID().toString()), now);
        }
        return targets.size();
    }

    private void purgeUserData(User user) {
        Long userId = user.getId();

        tourReceiptRepository.deleteAll(tourReceiptRepository.findAllBySavedCourseUserId(userId));
        savedCourseRepository.deleteAll(savedCourseRepository.findAllByUserId(userId));

        List<Image> images = imageRepository.findAllByUploaderId(userId);
        List<String> storageKeys = images.stream().map(Image::getStorageKey).toList();
        imageRepository.deleteAll(images);

        generatedCourseRepository.deleteAll(generatedCourseRepository.findAllByUserId(userId));
        recommendedRegionRepository.deleteAll(
            recommendedRegionRepository.findAllByUserIdOrderByDisplayOrder(userId));
        propensityRepository.findByUserId(userId).ifPresent(propensityRepository::delete);

        refreshTokenRepository.deleteByUserId(userId);
        signupVerificationTokenRepository.deleteByUserId(userId);
        passwordResetTokenRepository.deleteByUserId(userId);

        deleteStorageObjects(storageKeys);
    }

    private void deleteStorageObjects(List<String> storageKeys) {
        for (String key : storageKeys) {
            try {
                imageStorage.delete(key);
            } catch (RuntimeException exception) {
                log.warn("탈퇴 회원 이미지 삭제 실패. key={}", key, exception);
            }
        }
    }
}
