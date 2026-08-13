package live.lbtrip.domain.tourism.model.vo;

import java.util.Map;

import live.lbtrip.domain.tourism.model.enums.CategoryGroup;

public record RegionStats(
    int totalCount,
    int sampleSize,
    Map<Integer, Integer> typeCounts,
    Map<CategoryGroup, Integer> groupCounts
) {
}
