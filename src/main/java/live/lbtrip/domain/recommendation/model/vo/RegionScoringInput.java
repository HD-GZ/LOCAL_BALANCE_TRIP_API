package live.lbtrip.domain.recommendation.model.vo;

import live.lbtrip.domain.tourism.client.dto.RegionStats;

public record RegionScoringInput(
    RegionStats stats,
    double recentOutsiderVisitors
) {

    public static RegionScoringInput of(RegionStats stats, double recentOutsiderVisitors) {
        return new RegionScoringInput(stats, recentOutsiderVisitors);
    }
}
