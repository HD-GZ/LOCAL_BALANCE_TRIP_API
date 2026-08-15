package live.lbtrip.domain.user.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserAnonymizationProcessor {

    private final UserFinder userFinder;
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

    @Transactional
    public List<String> anonymize(Long userId) {
        User user = userFinder.findById(userId);

        List<String> storageKeys = purgeUserData(userId);
        user.anonymize(passwordEncoder.encode(UUID.randomUUID().toString()), LocalDateTime.now());

        return storageKeys;
    }

    private List<String> purgeUserData(Long userId) {
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

        return storageKeys;
    }
}
