package live.lbtrip.domain.tourism.client.dto;

import java.util.Map;

import live.lbtrip.domain.tourism.model.entity.TourRegionStats;

public record RegionStats(
    String regionName,
    String ldongRegnCd,
    String ldongSignguCd,
    int totalCount,
    int sampleSize,
    Map<Integer, Integer> typeCounts
) {

    public static RegionStats of(TourRegionStats stats, String regionName) {
        return new RegionStats(
            regionName, stats.getLdongRegnCd(), stats.getLdongSignguCd(),
            stats.getTotalCount(), stats.getSampleSize(), stats.toTypeCounts());
    }

    public double typeRatio(int contentTypeId) {
        if (sampleSize == 0) {
            return 0.0;
        }
        return typeCounts.getOrDefault(contentTypeId, 0) / (double) sampleSize;
    }
}
