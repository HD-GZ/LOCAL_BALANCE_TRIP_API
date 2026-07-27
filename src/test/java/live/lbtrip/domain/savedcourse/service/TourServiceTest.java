package live.lbtrip.domain.savedcourse.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import live.lbtrip.domain.savedcourse.dto.response.TourProgressResponse;
import live.lbtrip.domain.savedcourse.dto.response.TourSummaryResponse;
import live.lbtrip.domain.savedcourse.model.SavedCourseStatus;
import live.lbtrip.domain.savedcourse.model.entity.SavedCourse;

@ExtendWith(MockitoExtension.class)
class TourServiceTest {

    private static final Long USER_ID = 1L;
    private static final Long SAVED_COURSE_ID = 2L;
    private static final Long PLACE_ID = 3L;

    @Mock
    private SavedCourseFinder savedCourseFinder;

    @Mock
    private SavedCourse savedCourse;

    @InjectMocks
    private TourService tourService;

    @Nested
    class 투어_시작 {

        @Test
        void 저장_코스의_투어를_시작한다() {
            when(savedCourseFinder.findByIdAndUserId(SAVED_COURSE_ID, USER_ID))
                .thenReturn(savedCourse);
            when(savedCourse.getId()).thenReturn(SAVED_COURSE_ID);
            when(savedCourse.getStatus()).thenReturn(SavedCourseStatus.TRAVELING);
            when(savedCourse.getPlaces()).thenReturn(List.of());

            TourProgressResponse result = tourService.startTour(USER_ID, SAVED_COURSE_ID);

            verify(savedCourse).startTour();
            assertThat(result.savedCourseId()).isEqualTo(SAVED_COURSE_ID);
            assertThat(result.status()).isEqualTo(SavedCourseStatus.TRAVELING);
        }
    }

    @Nested
    class 장소_체크인 {

        @Test
        void 저장_코스의_장소를_체크인한다() {
            when(savedCourseFinder.findByIdAndUserId(SAVED_COURSE_ID, USER_ID))
                .thenReturn(savedCourse);

            tourService.checkIn(USER_ID, SAVED_COURSE_ID, PLACE_ID);

            verify(savedCourse).checkInPlace(PLACE_ID);
        }
    }

    @Nested
    class 투어_종료 {

        @Test
        void 투어를_종료하고_요약을_반환한다() {
            when(savedCourseFinder.findByIdAndUserId(SAVED_COURSE_ID, USER_ID))
                .thenReturn(savedCourse);
            when(savedCourse.endTour()).thenReturn(true);
            when(savedCourse.countVisitedPlaces()).thenReturn(2);
            when(savedCourse.getPlaces()).thenReturn(List.of(
                org.mockito.Mockito.mock(live.lbtrip.domain.savedcourse.model.entity.SavedCoursePlace.class),
                org.mockito.Mockito.mock(live.lbtrip.domain.savedcourse.model.entity.SavedCoursePlace.class)
            ));
            when(savedCourse.tourDurationMinutes()).thenReturn(60L);

            TourSummaryResponse result = tourService.endTour(USER_ID, SAVED_COURSE_ID);

            assertThat(result.completed()).isTrue();
            assertThat(result.visitedPlaceCount()).isEqualTo(2);
            assertThat(result.totalPlaceCount()).isEqualTo(2);
            assertThat(result.durationMinutes()).isEqualTo(60);
        }
    }
}
