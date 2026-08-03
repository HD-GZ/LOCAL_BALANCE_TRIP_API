package live.lbtrip.domain.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

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
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import live.lbtrip.global.storage.enums.ImageDirectory;
import live.lbtrip.support.fixture.UserFixture;

@ExtendWith(MockitoExtension.class)
class UserAnonymizationProcessorTest {

    private static final Long USER_ID = 1L;

    @Mock
    private UserFinder userFinder;

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

    @InjectMocks
    private UserAnonymizationProcessor anonymizationProcessor;

    @Nested
    class 회원_단위_익명화 {

        @Test
        void 연관_데이터를_삭제하고_회원을_익명화한_뒤_이미지_키를_반환한다() {
            User user = UserFixture.withdrawnUser(USER_ID, LocalDateTime.now().minusDays(40));
            Image image = Image.create(user, ImageDirectory.RECEIPT, "receipts/key.jpg", "image/jpeg", 1024L);
            when(userFinder.findById(USER_ID)).thenReturn(user);
            when(imageRepository.findAllByUploaderId(USER_ID)).thenReturn(List.of(image));
            when(passwordEncoder.encode(anyString())).thenReturn("anonymized-password");

            List<String> storageKeys = anonymizationProcessor.anonymize(USER_ID);

            assertThat(storageKeys).containsExactly("receipts/key.jpg");
            assertThat(user.getName()).isEqualTo("탈퇴회원");
            assertThat(user.getPassword()).isEqualTo("anonymized-password");
            assertThat(user.getDeletedAt()).isNotNull();
            verify(tourReceiptRepository).findAllBySavedCourseUserId(USER_ID);
            verify(savedCourseRepository).findAllByUserId(USER_ID);
            verify(imageRepository).deleteAll(List.of(image));
            verify(generatedCourseRepository).findAllByUserId(USER_ID);
            verify(recommendedRegionRepository).findAllByUserIdOrderByDisplayOrder(USER_ID);
            verify(propensityRepository).findByUserId(USER_ID);
            verify(refreshTokenRepository).deleteByUserId(USER_ID);
            verify(signupVerificationTokenRepository).deleteByUserId(USER_ID);
            verify(passwordResetTokenRepository).deleteByUserId(USER_ID);
        }

        @Test
        void 존재하지_않는_회원이면_예외가_발생한다() {
            when(userFinder.findById(USER_ID)).thenThrow(BusinessException.of(ErrorCode.USER_NOT_FOUND));

            assertThatThrownBy(() -> anonymizationProcessor.anonymize(USER_ID))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USER_NOT_FOUND);
        }
    }
}
