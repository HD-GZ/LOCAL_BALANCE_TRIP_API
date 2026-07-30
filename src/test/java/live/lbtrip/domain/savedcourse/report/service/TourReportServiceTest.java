package live.lbtrip.domain.savedcourse.report.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import live.lbtrip.domain.savedcourse.course.service.SavedCourseFinder;
import live.lbtrip.domain.savedcourse.model.entity.SavedCourse;
import live.lbtrip.domain.savedcourse.report.dto.response.TourReportResponse;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;

@ExtendWith(MockitoExtension.class)
class TourReportServiceTest {

    private static final Long USER_ID = 1L;
    private static final Long SAVED_COURSE_ID = 2L;

    @Mock
    private SavedCourseFinder savedCourseFinder;

    @Mock
    private SavedCourse savedCourse;

    @InjectMocks
    private TourReportService tourReportService;

    @Nested
    class 리포트_조회 {

        @Test
        void 투어_리포트를_조회한다() {
            LocalDateTime endedAt = LocalDateTime.of(2026, 7, 17, 15, 30);
            when(savedCourseFinder.findByIdAndUserId(SAVED_COURSE_ID, USER_ID))
                .thenReturn(savedCourse);
            when(savedCourse.getCourseName()).thenReturn("공주 원도심 슬로우 투어");
            when(savedCourse.getImageUrl()).thenReturn("https://images.example.com/course.jpg");
            when(savedCourse.countVisitedPlaces()).thenReturn(5);
            when(savedCourse.tourDurationMinutes()).thenReturn(130L);
            when(savedCourse.calculateTotalReceiptAmount()).thenReturn(52000);
            when(savedCourse.getTourEndedAt()).thenReturn(endedAt);

            TourReportResponse result = tourReportService.getReport(USER_ID, SAVED_COURSE_ID);

            assertThat(result.courseName()).isEqualTo("공주 원도심 슬로우 투어");
            assertThat(result.imageUrl()).isEqualTo("https://images.example.com/course.jpg");
            assertThat(result.visitedPlaceCount()).isEqualTo(5);
            assertThat(result.durationMinutes()).isEqualTo(130L);
            assertThat(result.totalSpentAmount()).isEqualTo(52000);
            assertThat(result.tourEndedAt()).isEqualTo(endedAt);
        }

        @Test
        void 투어를_종료하지_않았으면_예외가_발생한다() {
            when(savedCourseFinder.findByIdAndUserId(SAVED_COURSE_ID, USER_ID))
                .thenReturn(savedCourse);
            doThrow(BusinessException.of(ErrorCode.TOUR_REPORT_NOT_AVAILABLE))
                .when(savedCourse).validateReportAvailable();

            assertThatThrownBy(() -> tourReportService.getReport(USER_ID, SAVED_COURSE_ID))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.TOUR_REPORT_NOT_AVAILABLE);
        }
    }
}
