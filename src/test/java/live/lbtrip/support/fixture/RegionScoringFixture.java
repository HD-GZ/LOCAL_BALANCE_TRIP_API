package live.lbtrip.support.fixture;

import java.util.Map;

import live.lbtrip.domain.recommendation.model.vo.RegionScoringInput;
import live.lbtrip.domain.tourism.client.dto.RegionStats;
import live.lbtrip.domain.tourism.model.enums.CategoryGroup;
import live.lbtrip.domain.tourism.model.enums.TourContentType;

public final class RegionScoringFixture {

    private RegionScoringFixture() {
    }

    public static RegionScoringInput 로컬실속_지역() {
        return RegionScoringInput.of(new RegionStats("로컬실속", "46", "710", 80, 100,
            Map.of(TourContentType.TOURIST_SPOT.getCode(), 5,
                TourContentType.RESTAURANT.getCode(), 20),
            Map.of(CategoryGroup.TRADITIONAL_MARKET, 8,
                CategoryGroup.NATURE_REST, 10)), 2_000);
    }

    public static RegionScoringInput 핫플럭셔리_지역() {
        return RegionScoringInput.of(new RegionStats("핫플럭셔리", "11", "110", 3000, 100,
            Map.of(TourContentType.TOURIST_SPOT.getCode(), 40,
                TourContentType.ACCOMMODATION.getCode(), 15),
            Map.of(CategoryGroup.LUXURY_SHOPPING, 12,
                CategoryGroup.CAFE, 8)), 300_000);
    }

    public static RegionScoringInput 체험활동_지역() {
        return RegionScoringInput.of(new RegionStats("체험활동", "51", "150", 400, 100,
            Map.of(TourContentType.LEPORTS.getCode(), 25),
            Map.of(CategoryGroup.EXPERIENCE_PLACE, 15)), 30_000);
    }

    public static RegionScoringInput 관람휴식_지역() {
        return RegionScoringInput.of(new RegionStats("관람휴식", "43", "760", 600, 100,
            Map.of(TourContentType.CULTURAL_FACILITY.getCode(), 20),
            Map.of(CategoryGroup.VIEWING_PLACE, 25,
                CategoryGroup.NATURE_REST, 15,
                CategoryGroup.EXHIBITION, 10)), 50_000);
    }
}
