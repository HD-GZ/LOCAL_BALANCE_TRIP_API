package live.lbtrip.domain.savedcourse.model.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import live.lbtrip.domain.savedcourse.model.enums.SavedCourseStatus;
import live.lbtrip.domain.user.model.User;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import live.lbtrip.support.fixture.RegionCandidateFixture;

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
            boolean completed = savedCourse.endTour(8400);

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

    @Nested
    class 걸은_거리와_탄소_절감량 {

        @Test
        void 투어를_종료하면_걸은_거리와_탄소_절감량을_계산할_수_있다() {
            SavedCourse savedCourse = savedCourse();
            savedCourse.addPlace(place(1L, 1));
            savedCourse.startTour();
            savedCourse.checkInPlace(1L);

            savedCourse.endTour(8400);

            assertThat(savedCourse.getWalkedDistanceMeters()).isEqualTo(8400);
            assertThat(savedCourse.walkedDistanceKm()).isEqualTo(8.4);
            assertThat(savedCourse.carbonReductionKg()).isEqualTo(1.8);
        }

        @Test
        void 탄소_절감량은_소수점_첫째_자리로_반올림한다() {
            SavedCourse savedCourse = savedCourse();
            savedCourse.addPlace(place(1L, 1));
            savedCourse.startTour();

            savedCourse.endTour(5000);

            assertThat(savedCourse.carbonReductionKg()).isEqualTo(1.1);
        }

        @Test
        void 걸은_거리가_기록되지_않았으면_null을_반환한다() {
            SavedCourse savedCourse = savedCourse();

            assertThat(savedCourse.walkedDistanceKm()).isNull();
            assertThat(savedCourse.carbonReductionKg()).isNull();
        }
    }

    @Nested
    class 리포트_검증 {

        @Test
        void 투어를_종료한_코스는_리포트를_조회할_수_있다() {
            SavedCourse savedCourse = savedCourse();
            savedCourse.addPlace(place(1L, 1));
            savedCourse.startTour();
            savedCourse.checkInPlace(1L);
            savedCourse.endTour(8400);

            savedCourse.validateReportAvailable();
        }

        @Test
        void 투어를_종료하지_않은_코스는_리포트를_조회할_수_없다() {
            SavedCourse savedCourse = savedCourse();

            assertErrorCode(
                savedCourse::validateReportAvailable,
                ErrorCode.TOUR_REPORT_NOT_AVAILABLE
            );
        }

        @Test
        void 투어를_다시_시작하면_리포트를_조회할_수_없다() {
            SavedCourse savedCourse = savedCourse();
            savedCourse.addPlace(place(1L, 1));
            savedCourse.startTour();
            savedCourse.endTour(8400);
            savedCourse.startTour();

            assertErrorCode(
                savedCourse::validateReportAvailable,
                ErrorCode.TOUR_REPORT_NOT_AVAILABLE
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
            RegionCandidateFixture.candidateWithId()
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
