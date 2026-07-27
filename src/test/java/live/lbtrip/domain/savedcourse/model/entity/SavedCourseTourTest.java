package live.lbtrip.domain.savedcourse.model.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import live.lbtrip.domain.savedcourse.model.SavedCourseStatus;
import live.lbtrip.domain.user.model.User;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;

class SavedCourseTourTest {

    @Nested
    class 투어_진행 {

        @Test
        void 투어를_시작하고_장소를_방문하면_완주할_수_있다() {
            SavedCourse savedCourse = savedCourse();
            SavedCoursePlace firstPlace = place(1L, 1);
            SavedCoursePlace secondPlace = place(2L, 2);
            savedCourse.addPlace(firstPlace);
            savedCourse.addPlace(secondPlace);

            savedCourse.startTour();
            savedCourse.checkInPlace(1L);
            savedCourse.checkInPlace(2L);
            boolean completed = savedCourse.endTour();

            assertThat(completed).isTrue();
            assertThat(savedCourse.getStatus()).isEqualTo(SavedCourseStatus.COMPLETED);
            assertThat(savedCourse.countVisitedPlaces()).isEqualTo(2);
            assertThat(firstPlace.getVisitedAt()).isNotNull();
        }

        @Test
        void 같은_장소를_다시_체크인해도_최초_방문_시각을_유지한다() {
            SavedCourse savedCourse = savedCourse();
            SavedCoursePlace place = place(1L, 1);
            savedCourse.addPlace(place);
            savedCourse.startTour();
            savedCourse.checkInPlace(1L);
            var firstVisitedAt = place.getVisitedAt();

            savedCourse.checkInPlace(1L);

            assertThat(place.getVisitedAt()).isEqualTo(firstVisitedAt);
        }
    }

    @Nested
    class 투어_예외 {

        @Test
        void 완주한_코스는_다시_시작할_수_없다() {
            SavedCourse savedCourse = savedCourse();
            savedCourse.changeStatus(SavedCourseStatus.COMPLETED);

            assertErrorCode(savedCourse::startTour, ErrorCode.TOUR_ALREADY_COMPLETED);
        }

        @Test
        void 진행_중이_아니면_체크인할_수_없다() {
            SavedCourse savedCourse = savedCourse();
            savedCourse.addPlace(place(1L, 1));

            assertErrorCode(() -> savedCourse.checkInPlace(1L), ErrorCode.TOUR_NOT_IN_PROGRESS);
        }

        @Test
        void 코스에_없는_장소는_체크인할_수_없다() {
            SavedCourse savedCourse = savedCourse();
            savedCourse.addPlace(place(1L, 1));
            savedCourse.startTour();

            assertErrorCode(
                () -> savedCourse.checkInPlace(2L),
                ErrorCode.SAVED_COURSE_PLACE_NOT_FOUND
            );
        }
    }

    private SavedCourse savedCourse() {
        return SavedCourse.create(
            mock(User.class),
            1L,
            "공주 원도심 슬로우 투어",
            "충청남도 공주시",
            "천천히 걷는 코스",
            null,
            "44",
            "150"
        );
    }

    private SavedCoursePlace place(Long id, int visitOrder) {
        SavedCoursePlace place = SavedCoursePlace.create(
            visitOrder,
            "장소 " + visitOrder,
            "소개",
            null,
            36.0,
            127.0,
            null,
            false,
            null
        );
        ReflectionTestUtils.setField(place, "id", id);
        return place;
    }

    private void assertErrorCode(Runnable action, ErrorCode errorCode) {
        assertThatThrownBy(action::run)
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(errorCode);
    }
}
