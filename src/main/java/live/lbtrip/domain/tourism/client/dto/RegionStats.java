package live.lbtrip.domain.tourism.client.dto;

import java.util.Map;

import live.lbtrip.domain.region.model.RegionCandidate;
import live.lbtrip.domain.tourism.model.entity.TourRegionStats;
import live.lbtrip.domain.tourism.model.enums.CategoryGroup;

public record RegionStats(
    Long regionCandidateId,
    String regionName,
    int totalCount,
    int sampleSize,
    Map<Integer, Integer> typeCounts,
    Map<CategoryGroup, Integer> groupCounts
) {

    public static RegionStats of(TourRegionStats stats, RegionCandidate candidate) {
        return new RegionStats(
            candidate.getId(), candidate.getName(),
            stats.getTotalCount(), stats.getSampleSize(), stats.toTypeCounts(), stats.toGroupCounts());
    }

    public double typeRatio(int contentTypeId) {
        if (sampleSize == 0) {
            return 0.0;
        }
        return typeCounts.getOrDefault(contentTypeId, 0) / (double) sampleSize;
    }

    public double groupRatio(CategoryGroup group) {
        if (sampleSize == 0) {
            return 0.0;
        }
        return groupCounts.getOrDefault(group, 0) / (double) sampleSize;
    }
}
