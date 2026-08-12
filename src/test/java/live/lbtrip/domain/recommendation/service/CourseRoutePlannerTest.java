package live.lbtrip.domain.recommendation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;

import live.lbtrip.domain.recommendation.model.vo.RoutedPlace;
import live.lbtrip.domain.tourism.model.entity.TourPlace;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import live.lbtrip.support.fixture.RegionCandidateFixture;

class CourseRoutePlannerTest {

    private final CourseRoutePlanner courseRoutePlanner = new CourseRoutePlanner();

    @Test
    void 총_이동거리가_최소인_순서로_정렬하고_구간_도보_시간을_붙인다() {
        TourPlace start = place("1", 127.0, 35.0);
        TourPlace middle = place("2", 127.0, 35.008993);
        TourPlace end = place("3", 127.0, 35.017986);

        List<RoutedPlace> result = courseRoutePlanner.plan(List.of(start, end, middle));

        assertThat(result).extracting(RoutedPlace::place).containsExactly(start, middle, end);
        assertThat(result).extracting(RoutedPlace::walkMinutes).containsExactly(null, 15, 15);
    }

    @Test
    void 같은_좌표_구간은_최소_1분으로_계산한다() {
        TourPlace first = place("1", 127.0, 35.0);
        TourPlace second = place("2", 127.0, 35.0);

        List<RoutedPlace> result = courseRoutePlanner.plan(List.of(first, second));

        assertThat(result.get(1).walkMinutes()).isEqualTo(1);
    }

    @Test
    void 두_곳_미만이면_순서를_바꾸지_않는다() {
        TourPlace only = place("1", 126.0, 35.0);

        List<RoutedPlace> result = courseRoutePlanner.plan(List.of(only));

        assertThat(result).singleElement().satisfies(routed -> {
            assertThat(routed.place()).isEqualTo(only);
            assertThat(routed.walkMinutes()).isNull();
        });
    }

    @Test
    void 좌표_없는_장소가_있으면_입력_순서를_유지하고_해당_구간_도보_시간은_null이다() {
        TourPlace first = place("1", 126.02, 35.0);
        TourPlace noCoordinate = place("2", null, null);
        TourPlace third = place("3", 126.00, 35.0);

        List<RoutedPlace> result = courseRoutePlanner.plan(List.of(first, noCoordinate, third));

        assertThat(result).extracting(RoutedPlace::place).containsExactly(first, noCoordinate, third);
        assertThat(result).extracting(RoutedPlace::walkMinutes).containsExactly(null, null, null);
    }

    @Test
    void 장소가_5곳을_넘으면_예외를_던진다() {
        List<TourPlace> tooMany = List.of(
            place("1", 126.0, 35.0), place("2", 126.1, 35.0), place("3", 126.2, 35.0),
            place("4", 126.3, 35.0), place("5", 126.4, 35.0), place("6", 126.5, 35.0));

        assertThatThrownBy(() -> courseRoutePlanner.plan(tooMany))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.RECOMMENDATION_GENERATION_FAILED);
    }

    private TourPlace place(String contentId, Double longitude, Double latitude) {
        return TourPlace.create(contentId, RegionCandidateFixture.candidateWithId(), 12,
            "장소" + contentId, null, longitude, latitude, 1);
    }
}
