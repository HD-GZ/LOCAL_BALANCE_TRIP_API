package live.lbtrip.domain.recommendation.client.dto;

import java.util.Map;

public record RegionStats(
    String regionName,
    String ldongRegnCd,
    String ldongSignguCd,
    int totalCount,
    Map<Integer, Integer> typeCounts
) {

    public int sampleSize() {
        return typeCounts.values().stream().mapToInt(Integer::intValue).sum();
    }

    public double typeRatio(int contentTypeId) {
        int sample = sampleSize();
        if (sample == 0) {
            return 0.0;
        }
        return typeCounts.getOrDefault(contentTypeId, 0) / (double) sample;
    }
}
