package live.lbtrip.domain.savedcourse.course.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import live.lbtrip.domain.incentive.service.IncentiveFinder;
import live.lbtrip.domain.recommendation.model.entity.GeneratedCourse;
import live.lbtrip.domain.recommendation.service.GeneratedCourseFinder;
import live.lbtrip.domain.user.model.User;
import live.lbtrip.domain.user.service.UserFinder;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;

@ExtendWith(MockitoExtension.class)
class SavedCourseServiceTest {

    private static final Long USER_ID = 1L;
    private static final Long COURSE_ID = 2L;

    @Mock
    private SavedCourseFinder savedCourseFinder;

    @Mock
    private IncentiveFinder incentiveFinder;

    @Mock
    private SaveCourseManager saveCourseManager;

    @Mock
    private SaveCourseValidator saveCourseValidator;

    @Mock
    private GeneratedCourseFinder generatedCourseFinder;

    @Mock
    private UserFinder userFinder;

    @Mock
    private GeneratedCourse generatedCourse;

    @Mock
    private User user;

    @InjectMocks
    private SavedCourseService savedCourseService;

    @Nested
    class 저장 {

        @Test
        void 추천_코스를_저장한다() {
            when(generatedCourseFinder.findByIdAndUserId(COURSE_ID, USER_ID))
                .thenReturn(generatedCourse);
            when(userFinder.findById(USER_ID)).thenReturn(user);

            savedCourseService.saveCourse(USER_ID, COURSE_ID);

            verify(saveCourseValidator).validateNew(USER_ID, COURSE_ID);
            verify(saveCourseManager).add(generatedCourse, user);
        }

        @Test
        void 이미_저장한_코스면_예외를_던진다() {
            doThrow(BusinessException.of(ErrorCode.DUPLICATE_SAVE_COURSE))
                .when(saveCourseValidator).validateNew(USER_ID, COURSE_ID);

            assertThatThrownBy(() -> savedCourseService.saveCourse(USER_ID, COURSE_ID))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.DUPLICATE_SAVE_COURSE);

            verify(generatedCourseFinder, never()).findByIdAndUserId(COURSE_ID, USER_ID);
            verify(saveCourseManager, never()).add(generatedCourse, user);
        }

        @Test
        void 추천_코스가_없으면_예외를_던진다() {
            when(generatedCourseFinder.findByIdAndUserId(COURSE_ID, USER_ID))
                .thenThrow(BusinessException.of(ErrorCode.COURSE_NOT_FOUND));

            assertThatThrownBy(() -> savedCourseService.saveCourse(USER_ID, COURSE_ID))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.COURSE_NOT_FOUND);

            verify(userFinder, never()).findById(USER_ID);
            verify(saveCourseManager, never()).add(generatedCourse, user);
        }

        @Test
        void 사용자가_없으면_예외를_던진다() {
            when(generatedCourseFinder.findByIdAndUserId(COURSE_ID, USER_ID))
                .thenReturn(generatedCourse);
            when(userFinder.findById(USER_ID))
                .thenThrow(BusinessException.of(ErrorCode.USER_NOT_FOUND));

            assertThatThrownBy(() -> savedCourseService.saveCourse(USER_ID, COURSE_ID))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USER_NOT_FOUND);

            verify(saveCourseManager, never()).add(generatedCourse, user);
        }
    }
}
