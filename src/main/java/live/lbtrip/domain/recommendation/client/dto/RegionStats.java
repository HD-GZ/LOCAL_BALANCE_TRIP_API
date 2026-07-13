package live.lbtrip.domain.recommendation.client.dto;

import java.util.Map;

import live.lbtrip.domain.recommendation.model.entity.RegionCandidate;

public record RegionStats(
    RegionCandidate candidate,
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
