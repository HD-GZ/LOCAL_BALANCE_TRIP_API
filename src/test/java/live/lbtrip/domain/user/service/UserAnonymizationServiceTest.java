package live.lbtrip.domain.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import live.lbtrip.domain.auth.repository.PasswordResetTokenRepository;
import live.lbtrip.domain.auth.repository.RefreshTokenRepository;
import live.lbtrip.domain.auth.repository.SignupVerificationTokenRepository;
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
import live.lbtrip.support.fixture.UserFixture;

@ExtendWith(MockitoExtension.class)
class UserAnonymizationServiceTest {

    private static final Duration GRACE_PERIOD = Duration.ofDays(30);

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private SignupVerificationTokenRepository signupVerificationTokenRepository;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private PropensityRepository propensityRepository;

    @Mock
    private RecommendedRegionRepository recommendedRegionRepository;

    @Mock
    private GeneratedCourseRepository generatedCourseRepository;

    @Mock
    private SavedCourseRepository savedCourseRepository;

    @Mock
    private TourReceiptRepository tourReceiptRepository;

    @Mock
    private ImageRepository imageRepository;

    @Mock
    private ImageStorage imageStorage;

    private UserAnonymizationService anonymizationService;

    @BeforeEach
    void setUp() {
        anonymizationService = new UserAnonymizationService(
            userRepository,
            passwordEncoder,
            refreshTokenRepository,
            signupVerificationTokenRepository,
            passwordResetTokenRepository,
            propensityRepository,
            recommendedRegionRepository,
            generatedCourseRepository,
            savedCourseRepository,
            tourReceiptRepository,
            imageRepository,
            imageStorage,
            GRACE_PERIOD
        );
    }

    @Nested
    class 만료_회원_익명화 {

        @Test
        void 유예기간이_지난_탈퇴_회원을_익명화하고_연관_데이터를_삭제한다() {
            User user = UserFixture.activeUser();
            user.withdraw(LocalDateTime.now().minusDays(40));
            when(userRepository.findAllByStatusAndDeletedAtIsNullAndWithdrawnAtBefore(
                any(UserStatus.class), any(LocalDateTime.class)))
                .thenReturn(List.of(user));
            when(passwordEncoder.encode(anyString())).thenReturn("anonymized-password");

            int count = anonymizationService.anonymizeExpiredUsers();

            assertThat(count).isEqualTo(1);
            assertThat(user.getName()).isEqualTo("탈퇴회원");
            assertThat(user.getDeletedAt()).isNotNull();
            verify(tourReceiptRepository).findAllBySavedCourseUserId(user.getId());
            verify(savedCourseRepository).findAllByUserId(user.getId());
            verify(imageRepository).findAllByUploaderId(user.getId());
            verify(generatedCourseRepository).findAllByUserId(user.getId());
            verify(recommendedRegionRepository).findAllByUserIdOrderByDisplayOrder(user.getId());
            verify(propensityRepository).findByUserId(user.getId());
            verify(refreshTokenRepository).deleteByUserId(user.getId());
            verify(signupVerificationTokenRepository).deleteByUserId(user.getId());
            verify(passwordResetTokenRepository).deleteByUserId(user.getId());
        }

        @Test
        void 대상이_없으면_아무것도_처리하지_않는다() {
            when(userRepository.findAllByStatusAndDeletedAtIsNullAndWithdrawnAtBefore(
                any(UserStatus.class), any(LocalDateTime.class)))
                .thenReturn(List.of());

            int count = anonymizationService.anonymizeExpiredUsers();

            assertThat(count).isZero();
        }
    }
}
