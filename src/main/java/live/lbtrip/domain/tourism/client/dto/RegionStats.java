package live.lbtrip.domain.tourism.client.dto;

import java.util.Map;

import live.lbtrip.domain.tourism.model.enums.CategoryGroup;

public record RegionStats(
    Long regionCandidateId,
    String regionName,
    int totalCount,
    int sampleSize,
    Map<Integer, Integer> typeCounts,
    Map<CategoryGroup, Integer> groupCounts
) {
}
