package live.lbtrip.domain.tourism.model.vo;

import java.util.Locale;
import java.util.Map;

import live.lbtrip.domain.region.model.RegionCandidate;
import live.lbtrip.domain.tourism.model.entity.TourRegionStats;
import live.lbtrip.domain.tourism.model.enums.CategoryGroup;

public record RegionMetrics(
    Long regionCandidateId,
    String regionName,
    String regionNameEn,
    int totalCount,
    int sampleSize,
    Map<Integer, Integer> typeCounts,
    Map<CategoryGroup, Integer> groupCounts,
    double recentOutsiderVisitors
) {

    public static RegionMetrics of(
        TourRegionStats stats, RegionCandidate candidate, double recentOutsiderVisitors
    ) {
        return new RegionMetrics(
            candidate.getId(),
            candidate.getName(),
            candidate.getNameEn(),
            stats.getTotalCount(),
            stats.getSampleSize(),
            stats.toTypeCounts(),
            stats.toGroupCounts(),
            recentOutsiderVisitors
        );
    }

    public String regionNameFor(Locale locale) {
        if (Locale.ENGLISH.getLanguage().equals(locale.getLanguage()) && regionNameEn != null && !regionNameEn.isBlank()) {
            return regionNameEn;
        }
        return regionName;
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
