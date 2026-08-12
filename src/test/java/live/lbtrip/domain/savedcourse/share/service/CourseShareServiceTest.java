package live.lbtrip.domain.savedcourse.share.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import live.lbtrip.domain.incentive.service.IncentiveFinder;
import live.lbtrip.domain.savedcourse.course.dto.response.SavedCourseDetailResponse;
import live.lbtrip.domain.savedcourse.course.service.SavedCourseFinder;
import live.lbtrip.domain.savedcourse.model.entity.SavedCourse;
import live.lbtrip.domain.savedcourse.share.model.entity.CourseShareToken;
import live.lbtrip.domain.savedcourse.share.repository.CourseShareTokenRepository;
import live.lbtrip.domain.savedcourse.share.dto.response.ShareTokenResponse;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import live.lbtrip.support.fixture.CourseShareFixture;

@ExtendWith(MockitoExtension.class)
class CourseShareServiceTest {

    private static final Long USER_ID = 1L;
    private static final Long SAVED_COURSE_ID = 3L;

    @Mock
    private SavedCourseFinder savedCourseFinder;

    @Mock
    private IncentiveFinder incentiveFinder;

    @Mock
    private CourseShareTokenFinder courseShareTokenFinder;

    @Mock
    private CourseShareTokenRepository courseShareTokenRepository;

    @InjectMocks
    private CourseShareService courseShareService;

    @Nested
    class 공유_토큰_발급 {

        @Test
        void 새_토큰을_저장하고_토큰과_만료시각을_반환한다() {
            SavedCourse savedCourse = CourseShareFixture.savedCourse();
            when(savedCourseFinder.findByIdAndUserId(SAVED_COURSE_ID, USER_ID))
                .thenReturn(savedCourse);
            when(courseShareTokenRepository.save(any(CourseShareToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            ShareTokenResponse response = courseShareService.issueShareToken(USER_ID, SAVED_COURSE_ID);

            ArgumentCaptor<CourseShareToken> captor = ArgumentCaptor.forClass(CourseShareToken.class);
            verify(courseShareTokenRepository).save(captor.capture());
            CourseShareToken saved = captor.getValue();
            assertThat(saved.getSavedCourse()).isEqualTo(savedCourse);
            assertThat(saved.getToken()).hasSize(36);
            assertThat(saved.getExpiresAt()).isAfter(LocalDateTime.now().plusDays(6));
            assertThat(saved.getExpiresAt()).isBefore(LocalDateTime.now().plusDays(8));
            assertThat(response.token()).isEqualTo(saved.getToken());
            assertThat(response.expiresAt()).isEqualTo(saved.getExpiresAt());
        }

        @Test
        void 본인_코스가_아니면_예외를_던진다() {
            doThrow(BusinessException.of(ErrorCode.SAVED_COURSE_NOT_FOUND))
                .when(savedCourseFinder).findByIdAndUserId(SAVED_COURSE_ID, USER_ID);

            assertThatThrownBy(() -> courseShareService.issueShareToken(USER_ID, SAVED_COURSE_ID))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.SAVED_COURSE_NOT_FOUND);
        }
    }

    @Nested
    class 공유_코스_조회 {

        @Test
        void 유효한_토큰이면_코스_상세를_반환한다() {
            CourseShareToken shareToken = CourseShareFixture.shareToken();
            when(courseShareTokenFinder.findByToken(CourseShareFixture.TOKEN))
                .thenReturn(shareToken);
            when(incentiveFinder.findAllByRegion(
                shareToken.getSavedCourse().getLdongRegnCd(),
                shareToken.getSavedCourse().getLdongSignguCd()))
                .thenReturn(List.of());

            SavedCourseDetailResponse response = courseShareService.getSharedCourseDetail(CourseShareFixture.TOKEN);

            assertThat(response.savedCourseId()).isEqualTo(CourseShareFixture.SAVED_COURSE_ID);
            assertThat(response.benefits()).isEmpty();
        }

        @Test
        void 토큰이_없으면_예외를_던진다() {
            doThrow(BusinessException.of(ErrorCode.SHARE_TOKEN_NOT_FOUND))
                .when(courseShareTokenFinder).findByToken(CourseShareFixture.TOKEN);

            assertThatThrownBy(() -> courseShareService.getSharedCourseDetail(CourseShareFixture.TOKEN))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.SHARE_TOKEN_NOT_FOUND);
        }

        @Test
        void 만료된_토큰이면_예외를_던진다() {
            CourseShareToken shareToken = CourseShareToken.create(
                CourseShareFixture.savedCourse(),
                CourseShareFixture.TOKEN,
                LocalDateTime.now().minusSeconds(1));
            when(courseShareTokenFinder.findByToken(CourseShareFixture.TOKEN))
                .thenReturn(shareToken);

            assertThatThrownBy(() -> courseShareService.getSharedCourseDetail(CourseShareFixture.TOKEN))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.SHARE_TOKEN_EXPIRED);
        }
    }
}
