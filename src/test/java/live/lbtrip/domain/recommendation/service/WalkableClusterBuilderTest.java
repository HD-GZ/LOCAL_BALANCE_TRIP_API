package live.lbtrip.domain.recommendation.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import live.lbtrip.domain.recommendation.model.vo.WalkableCluster;
import live.lbtrip.domain.region.model.RegionCandidate;
import live.lbtrip.domain.tourism.model.entity.TourPlace;
import live.lbtrip.global.config.RecommendationProperties;
import live.lbtrip.support.fixture.RecommendationFixture;
import live.lbtrip.support.fixture.RegionCandidateFixture;

class WalkableClusterBuilderTest {

    private static final double LAT = 35.3;
    private static final double LON = 126.9;
    private static final double ONE_KM_LAT = 0.009;

    private final RegionCandidate candidate = RegionCandidateFixture.candidateWithId();

    private WalkableClusterBuilder builder(Integer... radii) {
        return new WalkableClusterBuilder(new RecommendationProperties(3, 3, List.of(radii)));
    }

    private TourPlace place(String contentId, double lat, double lon) {
        return TourPlace.create(Locale.KOREAN, contentId, candidate, 12, "장소" + contentId,
            RecommendationFixture.IMAGE_URL, lon, lat, 1);
    }

    @Nested
    class 클러스터_구성 {

        @Test
        void 반경_안에_모인_장소를_하나의_클러스터로_묶고_멀리_떨어진_장소는_제외한다() {
            List<TourPlace> places = List.of(
                place("1", LAT, LON),
                place("2", LAT + ONE_KM_LAT * 0.5, LON),
                place("3", LAT + ONE_KM_LAT, LON),
                place("9", LAT + ONE_KM_LAT * 20, LON));

            List<WalkableCluster> result = builder(1500).build(places);

            assertThat(result).singleElement().satisfies(cluster -> {
                assertThat(cluster.id()).isEqualTo("1");
                assertThat(cluster.places()).extracting(TourPlace::getContentId)
                    .containsExactly("1", "2", "3");
            });
        }

        @Test
        void 서로_떨어진_묶음은_별도_클러스터가_되고_큰_클러스터가_앞에_온다() {
            List<TourPlace> places = List.of(
                place("1", LAT, LON),
                place("2", LAT + ONE_KM_LAT * 0.5, LON),
                place("3", LAT + ONE_KM_LAT, LON),
                place("4", LAT + ONE_KM_LAT * 30, LON),
                place("5", LAT + ONE_KM_LAT * 30.5, LON),
                place("6", LAT + ONE_KM_LAT * 31, LON),
                place("7", LAT + ONE_KM_LAT * 31, LON + ONE_KM_LAT * 0.5));

            List<WalkableCluster> result = builder(1500).build(places);

            assertThat(result).hasSize(2);
            assertThat(result.get(0).id()).isEqualTo("1");
            assertThat(result.get(0).places()).extracting(TourPlace::getContentId)
                .containsExactly("4", "5", "6", "7");
            assertThat(result.get(1).id()).isEqualTo("2");
            assertThat(result.get(1).places()).extracting(TourPlace::getContentId)
                .containsExactly("1", "2", "3");
        }

        @Test
        void 세_곳_미만으로만_모이는_장소는_클러스터를_만들지_않는다() {
            List<TourPlace> places = List.of(
                place("1", LAT, LON),
                place("2", LAT + ONE_KM_LAT * 0.5, LON),
                place("3", LAT + ONE_KM_LAT * 10, LON));

            assertThat(builder(1500).build(places)).isEmpty();
        }

        @Test
        void 좌표가_없는_장소는_클러스터에서_제외한다() {
            List<TourPlace> places = List.of(
                place("1", LAT, LON),
                place("2", LAT + ONE_KM_LAT * 0.5, LON),
                place("3", LAT + ONE_KM_LAT, LON),
                TourPlace.create(Locale.KOREAN, "4", candidate, 12, "좌표없음",
                    RecommendationFixture.IMAGE_URL, null, null, 1));

            List<WalkableCluster> result = builder(1500).build(places);

            assertThat(result).singleElement().satisfies(cluster ->
                assertThat(cluster.places()).extracting(TourPlace::getContentId)
                    .containsExactly("1", "2", "3"));
        }
    }

    @Nested
    class 반경_폴백 {

        @Test
        void 첫_반경으로_코스_수만큼_채울_수_없으면_다음_반경으로_다시_묶는다() {
            List<TourPlace> places = List.of(
                place("1", LAT, LON),
                place("2", LAT + ONE_KM_LAT * 1.2, LON),
                place("3", LAT + ONE_KM_LAT * 2.4, LON),
                place("4", LAT + ONE_KM_LAT * 3.6, LON),
                place("5", LAT + ONE_KM_LAT * 4.8, LON),
                place("6", LAT + ONE_KM_LAT * 6.0, LON),
                place("7", LAT + ONE_KM_LAT * 7.2, LON),
                place("8", LAT + ONE_KM_LAT * 8.4, LON),
                place("9", LAT + ONE_KM_LAT * 9.6, LON));

            List<WalkableCluster> narrow = builder(1000).build(places);
            List<WalkableCluster> widened = builder(1000, 1500).build(places);

            assertThat(narrow).isEmpty();
            assertThat(widened).hasSize(3)
                .allSatisfy(cluster -> assertThat(cluster.size()).isEqualTo(3));
        }

        @Test
        void 첫_반경으로_충분하면_넓은_반경을_쓰지_않는다() {
            List<TourPlace> places = RecommendationFixture.manyTourPlaces(9);

            List<WalkableCluster> result = builder(1500, 100_000).build(places);

            assertThat(result).hasSize(1);
            assertThat(result.getFirst().size()).isEqualTo(9);
        }
    }
}
