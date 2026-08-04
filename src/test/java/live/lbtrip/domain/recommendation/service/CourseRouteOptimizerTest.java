package live.lbtrip.domain.recommendation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import live.lbtrip.domain.tourism.model.entity.TourPlace;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import live.lbtrip.support.fixture.RecommendationFixture;

class CourseRouteOptimizerTest {

    private final CourseRouteOptimizer optimizer = new CourseRouteOptimizer();

    @Test
    void 총_직선거리가_최소인_방문_순서를_선택한다() {
        TourPlace first = place("100", 127.000);
        TourPlace middle = place("200", 127.001);
        TourPlace last = place("300", 127.002);

        List<TourPlace> optimized = optimizer.optimize(List.of(middle, last, first));

        assertThat(optimized).extracting(TourPlace::getContentId)
            .containsExactly("100", "200", "300");
    }

    @Test
    void 총거리가_같으면_contentId_순열이_빠른_경로를_선택한다() {
        List<TourPlace> optimized = optimizer.optimize(List.of(
            place("300", 127.002), place("200", 127.001), place("100", 127.000)));

        assertThat(optimized).extracting(TourPlace::getContentId)
            .containsExactly("100", "200", "300");
    }

    @Test
    void 장소가_5개를_초과하면_추천_생성_예외를_던진다() {
        List<TourPlace> places = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            places.add(place(String.valueOf(100 + i), 127.0 + i * 0.001));
        }

        assertThatThrownBy(() -> optimizer.optimize(places))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.RECOMMENDATION_GENERATION_FAILED);
    }

    private TourPlace place(String contentId, double longitude) {
        return TourPlace.create(
            contentId,
            RecommendationFixture.LDONG_REGN_CD,
            RecommendationFixture.LDONG_SIGNGU_CD,
            12,
            "장소 " + contentId,
            RecommendationFixture.IMAGE_URL,
            longitude,
            35.0,
            Integer.parseInt(contentId)
        );
    }
}
