package live.lbtrip.domain.recommendation.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import live.lbtrip.domain.recommendation.model.vo.CourseCandidateGroup;
import live.lbtrip.domain.tourism.model.entity.TourPlace;
import live.lbtrip.support.fixture.RecommendationFixture;

class CourseCandidateClustererTest {

    private final CourseCandidateClusterer clusterer = new CourseCandidateClusterer();

    @Nested
    class 후보군_생성 {

        @Test
        void 장소_수에_따라_최대_3개_후보군을_생성한다() {
            List<TourPlace> places = linePlaces(9);

            List<CourseCandidateGroup> groups = clusterer.cluster(places);

            assertThat(groups).extracting(CourseCandidateGroup::id)
                .containsExactly("G1", "G2", "G3");
            assertThat(groups.get(0).candidates().getFirst().getContentId()).isEqualTo("104");
            assertThat(groups.get(1).candidates().getFirst().getContentId()).isEqualTo("100");
            assertThat(groups.get(2).candidates().getFirst().getContentId()).isEqualTo("108");
        }

        @Test
        void 각_후보군은_중심에서_가까운_장소_15개까지만_포함한다() {
            List<CourseCandidateGroup> groups = clusterer.cluster(linePlaces(20));

            assertThat(groups).allSatisfy(group -> assertThat(group.candidates()).hasSize(15));
        }

        @Test
        void 후보군_사이의_장소_중복을_허용한다() {
            List<CourseCandidateGroup> groups = clusterer.cluster(linePlaces(9));

            assertThat(groups.get(0).candidates()).containsAll(groups.get(1).candidates());
        }
    }

    @Nested
    class 좌표_검증 {

        @Test
        void 유효하지_않은_좌표는_후보에서_제외한다() {
            List<TourPlace> places = new ArrayList<>(linePlaces(3));
            places.add(place("200", null, 35.0, 20));
            places.add(place("201", 181.0, 35.0, 21));
            places.add(place("202", 127.0, Double.NaN, 22));

            List<CourseCandidateGroup> groups = clusterer.cluster(places);

            assertThat(groups).singleElement().satisfies(group ->
                assertThat(group.candidates()).extracting(TourPlace::getContentId)
                    .containsExactlyInAnyOrder("100", "101", "102"));
        }

        @Test
        void 유효한_좌표가_3개_미만이면_후보군을_생성하지_않는다() {
            assertThat(clusterer.cluster(linePlaces(2))).isEmpty();
        }
    }

    private List<TourPlace> linePlaces(int count) {
        List<TourPlace> places = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            places.add(place(String.valueOf(100 + i), 127.0 + i * 0.001, 35.0, i));
        }
        return places;
    }

    private TourPlace place(String contentId, Double longitude, Double latitude, int sortOrder) {
        return TourPlace.create(
            contentId,
            RecommendationFixture.LDONG_REGN_CD,
            RecommendationFixture.LDONG_SIGNGU_CD,
            12,
            "장소 " + contentId,
            RecommendationFixture.IMAGE_URL,
            longitude,
            latitude,
            sortOrder
        );
    }
}
