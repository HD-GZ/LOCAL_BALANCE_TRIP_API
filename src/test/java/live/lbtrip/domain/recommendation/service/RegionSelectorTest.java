package live.lbtrip.domain.recommendation.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import live.lbtrip.domain.propensity.model.Preference;
import live.lbtrip.domain.propensity.model.Propensity;
import live.lbtrip.domain.propensity.model.ValueConsumption;
import live.lbtrip.domain.tourism.model.enums.CategoryGroup;
import live.lbtrip.domain.tourism.model.enums.TourContentType;
import live.lbtrip.domain.tourism.model.vo.RegionMetrics;
import live.lbtrip.support.fixture.RegionMetricsFixture;
import live.lbtrip.support.fixture.UserFixture;

class RegionSelectorTest {

    private final RegionSelector regionSelector = new RegionSelector();

    @Nested
    class 양극_축_방향성 {

        @Test
        void 로컬_선호가_높으면_방문자가_적고_희소한_지역을_우선한다() {
            RegionMetrics hotspot = metrics("핫플", 2000,
                Map.of(TourContentType.TOURIST_SPOT.getCode(), 50), Map.of(), 100_000);
            RegionMetrics local = metrics("로컬", 100,
                Map.of(TourContentType.TOURIST_SPOT.getCode(), 5), Map.of(), 1_000);

            assertThat(top(preference(5, 3, 3, 3), List.of(hotspot, local))).isEqualTo("로컬");
            assertThat(top(preference(1, 3, 3, 3), List.of(hotspot, local))).isEqualTo("핫플");
        }

        @Test
        void 실속_소비가_높으면_시장이_많은_지역을_우선한다() {
            RegionMetrics luxury = metrics("럭셔리", 100, Map.of(),
                Map.of(CategoryGroup.LUXURY_SHOPPING, 10), 0);
            RegionMetrics frugal = metrics("실속", 100, Map.of(),
                Map.of(CategoryGroup.TRADITIONAL_MARKET, 10), 0);

            assertThat(top(preference(3, 5, 3, 3), List.of(luxury, frugal))).isEqualTo("실속");
            assertThat(top(preference(3, 1, 3, 3), List.of(luxury, frugal))).isEqualTo("럭셔리");
        }

        @Test
        void 생활체험_선호가_높으면_체험시설_지역을_관람형이면_관람시설_지역을_우선한다() {
            RegionMetrics viewing = metrics("관람", 100, Map.of(),
                Map.of(CategoryGroup.VIEWING_PLACE, 20), 0);
            RegionMetrics experience = metrics("체험", 100, Map.of(),
                Map.of(CategoryGroup.EXPERIENCE_PLACE, 20), 0);

            assertThat(top(preference(3, 3, 5, 3), List.of(viewing, experience))).isEqualTo("체험");
            assertThat(top(preference(3, 3, 1, 3), List.of(viewing, experience))).isEqualTo("관람");
        }

        @Test
        void 활동성이_높으면_레포츠_지역을_휴식형이면_자연휴양_지역을_우선한다() {
            RegionMetrics rest = metrics("휴양", 100, Map.of(),
                Map.of(CategoryGroup.NATURE_REST, 20), 0);
            RegionMetrics active = metrics("액티브", 100,
                Map.of(TourContentType.LEPORTS.getCode(), 20), Map.of(), 0);

            assertThat(top(preference(3, 3, 3, 5), List.of(rest, active))).isEqualTo("액티브");
            assertThat(top(preference(3, 3, 3, 1), List.of(rest, active))).isEqualTo("휴양");
        }

        @Test
        void 방문자_집중도만_다르면_로컬_선호는_한적한_지역을_우선한다() {
            RegionMetrics crowded = metrics("몰림", 500,
                Map.of(TourContentType.TOURIST_SPOT.getCode(), 10), Map.of(), 300_000);
            RegionMetrics middle = metrics("중간", 500,
                Map.of(TourContentType.TOURIST_SPOT.getCode(), 10), Map.of(), 100_000);
            RegionMetrics quiet = metrics("한적", 500,
                Map.of(TourContentType.TOURIST_SPOT.getCode(), 10), Map.of(), 1_000);

            assertThat(top(preference(5, 3, 3, 3), List.of(crowded, middle, quiet))).isEqualTo("한적");
            assertThat(top(preference(1, 3, 3, 3), List.of(crowded, middle, quiet))).isEqualTo("몰림");
        }
    }

    @Nested
    class 단극_축_방향성 {

        @Test
        void 숙소_투자가_높으면_숙박_비율이_높은_지역을_우선한다() {
            RegionMetrics few = metrics("숙박少", 100,
                Map.of(TourContentType.ACCOMMODATION.getCode(), 2), Map.of(), 0);
            RegionMetrics many = metrics("숙박多", 100,
                Map.of(TourContentType.ACCOMMODATION.getCode(), 30), Map.of(), 0);

            assertThat(top(consumption(5, 3, 3, 3), List.of(few, many))).isEqualTo("숙박多");
        }

        @Test
        void 음식_투자가_높으면_카페를_제외한_음식점_비율로_우선한다() {
            RegionMetrics cafeTown = metrics("카페촌", 100,
                Map.of(TourContentType.RESTAURANT.getCode(), 30),
                Map.of(CategoryGroup.CAFE, 25), 0);
            RegionMetrics foodTown = metrics("맛집촌", 100,
                Map.of(TourContentType.RESTAURANT.getCode(), 30),
                Map.of(CategoryGroup.CAFE, 2), 0);

            assertThat(top(consumption(3, 5, 3, 3), List.of(cafeTown, foodTown))).isEqualTo("맛집촌");
        }

        @Test
        void 체험_투자가_높으면_체험시설과_레포츠_비율로_우선한다() {
            RegionMetrics plain = metrics("일반", 100, Map.of(), Map.of(), 0);
            RegionMetrics experience = metrics("체험형", 100,
                Map.of(TourContentType.LEPORTS.getCode(), 10),
                Map.of(CategoryGroup.EXPERIENCE_PLACE, 10), 0);

            assertThat(top(consumption(3, 3, 5, 3), List.of(plain, experience))).isEqualTo("체험형");
        }

        @Test
        void 카페전시_투자가_높으면_카페와_전시_비율로_우선한다() {
            RegionMetrics plain = metrics("일반", 100, Map.of(), Map.of(), 0);
            RegionMetrics cafeArt = metrics("카페전시", 100, Map.of(),
                Map.of(CategoryGroup.CAFE, 10, CategoryGroup.EXHIBITION, 5), 0);

            assertThat(top(consumption(3, 3, 3, 5), List.of(plain, cafeArt))).isEqualTo("카페전시");
        }
    }

    @Nested
    class 가중치_균형과_엣지 {

        @Test
        void 서로_다른_축의_같은_점수_차는_같은_영향력을_가진다() {
            RegionMetrics frugalBest = metrics("실속우세", 100, Map.of(),
                Map.of(CategoryGroup.TRADITIONAL_MARKET, 10), 0);
            RegionMetrics stayBest = metrics("숙박우세", 100,
                Map.of(TourContentType.ACCOMMODATION.getCode(), 10), Map.of(), 0);
            Propensity propensity = Propensity.create(UserFixture.user(),
                Preference.of(3, 5, 3, 3, 3), ValueConsumption.of(5, 3, 3, 3, 3));

            List<RegionMetrics> selected = regionSelector.selectTop(
                propensity, List.of(frugalBest, stayBest), 2);

            assertThat(selected).extracting(RegionMetrics::regionName)
                .containsExactly("실속우세", "숙박우세");
        }

        @Test
        void 중립_성향의_동점은_입력_순서를_유지한다() {
            List<RegionMetrics> inputs = List.of(
                metrics("첫 지역", 10, Map.of(), Map.of(), 0),
                metrics("두 번째 지역", 20, Map.of(), Map.of(), 0));
            Propensity propensity = Propensity.create(UserFixture.user(),
                Preference.of(3, 3, 3, 3, 3), ValueConsumption.of(3, 3, 3, 3, 3));

            List<RegionMetrics> selected = regionSelector.selectTop(propensity, inputs, 2);

            assertThat(selected).extracting(RegionMetrics::regionName)
                .containsExactly("첫 지역", "두 번째 지역");
        }

        @Test
        void 지역이_limit보다_적으면_있는_만큼만_반환한다() {
            List<RegionMetrics> inputs = List.of(metrics("하나뿐", 10, Map.of(), Map.of(), 0));

            List<RegionMetrics> selected = regionSelector.selectTop(
                preference(5, 3, 3, 3), inputs, 5);

            assertThat(selected).extracting(RegionMetrics::regionName).containsExactly("하나뿐");
        }

        @Test
        void 샘플이_없는_지역은_비율_지표가_0으로_처리된다() {
            RegionMetrics empty = new RegionMetrics(99L, "무샘플", null, 0, 0, Map.of(), Map.of(), 0);
            RegionMetrics normal = metrics("정상", 100, Map.of(),
                Map.of(CategoryGroup.TRADITIONAL_MARKET, 10), 0);

            assertThat(top(preference(3, 5, 3, 3), List.of(empty, normal))).isEqualTo("정상");
        }
    }

    @Nested
    class 페르소나_시나리오 {

        private final List<RegionMetrics> 전체_지역 = List.of(
            RegionMetricsFixture.핫플럭셔리_지역(),
            RegionMetricsFixture.로컬실속_지역(),
            RegionMetricsFixture.체험활동_지역(),
            RegionMetricsFixture.관람휴식_지역());

        @Test
        void 로컬_실속_여행자는_로컬실속_지역을_가장_선호한다() {
            Propensity propensity = Propensity.create(UserFixture.user(),
                Preference.of(5, 5, 3, 3, 3), ValueConsumption.of(3, 3, 3, 3, 3));

            assertThat(regionSelector.selectTop(propensity, 전체_지역, 1))
                .singleElement().extracting(RegionMetrics::regionName).isEqualTo("로컬실속");
        }

        @Test
        void 핫플_럭셔리_여행자는_핫플럭셔리_지역을_가장_선호한다() {
            Propensity propensity = Propensity.create(UserFixture.user(),
                Preference.of(1, 1, 3, 3, 3), ValueConsumption.of(5, 3, 3, 3, 3));

            assertThat(regionSelector.selectTop(propensity, 전체_지역, 1))
                .singleElement().extracting(RegionMetrics::regionName).isEqualTo("핫플럭셔리");
        }

        @Test
        void 체험_활동_여행자는_체험활동_지역을_가장_선호한다() {
            Propensity propensity = Propensity.create(UserFixture.user(),
                Preference.of(3, 3, 5, 5, 3), ValueConsumption.of(3, 3, 5, 3, 3));

            assertThat(regionSelector.selectTop(propensity, 전체_지역, 1))
                .singleElement().extracting(RegionMetrics::regionName).isEqualTo("체험활동");
        }

        @Test
        void 관람_휴식_여행자는_관람휴식_지역을_가장_선호한다() {
            Propensity propensity = Propensity.create(UserFixture.user(),
                Preference.of(3, 3, 1, 1, 3), ValueConsumption.of(3, 3, 3, 3, 5));

            assertThat(regionSelector.selectTop(propensity, 전체_지역, 1))
                .singleElement().extracting(RegionMetrics::regionName).isEqualTo("관람휴식");
        }
    }

    private String top(Propensity propensity, List<RegionMetrics> metrics) {
        return regionSelector.selectTop(propensity, metrics, 1).getFirst().regionName();
    }

    private Propensity preference(int locality, int frugality, int experientiality, int vitality) {
        return Propensity.create(UserFixture.user(),
            Preference.of(locality, frugality, experientiality, vitality, 3),
            ValueConsumption.of(3, 3, 3, 3, 3));
    }

    private Propensity consumption(int accommodation, int food, int experience, int cafeExhibition) {
        return Propensity.create(UserFixture.user(),
            Preference.of(3, 3, 3, 3, 3),
            ValueConsumption.of(accommodation, food, experience, 3, cafeExhibition));
    }

    private RegionMetrics metrics(
        String name, int totalCount,
        Map<Integer, Integer> typeCounts, Map<CategoryGroup, Integer> groupCounts,
        double recentOutsiderVisitors
    ) {
        return new RegionMetrics(1L, name, null, totalCount, 100, typeCounts, groupCounts, recentOutsiderVisitors);
    }
}
