package live.lbtrip.domain.tourism.model.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;

import live.lbtrip.domain.tourism.model.enums.CategoryGroup;
import live.lbtrip.domain.tourism.model.enums.TourContentType;

class TourRegionStatsTest {

    @Test
    void 카테고리_그룹_카운트를_저장하고_복원한다() {
        TourRegionStats stats = TourRegionStats.create(
            "46", "710", 120, 100,
            Map.of(TourContentType.RESTAURANT.getCode(), 30),
            Map.of(CategoryGroup.CAFE, 7, CategoryGroup.TRADITIONAL_MARKET, 3));

        assertThat(stats.toGroupCounts())
            .containsEntry(CategoryGroup.CAFE, 7)
            .containsEntry(CategoryGroup.TRADITIONAL_MARKET, 3)
            .containsEntry(CategoryGroup.LUXURY_SHOPPING, 0);
    }

    @Test
    void 업데이트하면_그룹_카운트를_덮어쓴다() {
        TourRegionStats stats = TourRegionStats.create(
            "46", "710", 120, 100, Map.of(), Map.of(CategoryGroup.CAFE, 7));

        stats.update(130, 100, Map.of(), Map.of(CategoryGroup.CAFE, 9));

        assertThat(stats.toGroupCounts()).containsEntry(CategoryGroup.CAFE, 9);
    }
}
