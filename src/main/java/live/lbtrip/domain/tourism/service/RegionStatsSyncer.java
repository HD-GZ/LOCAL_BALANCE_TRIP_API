package live.lbtrip.domain.tourism.service;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import live.lbtrip.domain.region.model.RegionCandidate;
import live.lbtrip.domain.tourism.client.TourApiClient;
import live.lbtrip.domain.tourism.client.dto.AreaBasedItem;
import live.lbtrip.domain.tourism.client.dto.AreaBasedSample;
import live.lbtrip.domain.tourism.model.entity.TourRegionStats;
import live.lbtrip.domain.tourism.model.enums.CategoryGroup;
import live.lbtrip.domain.tourism.model.vo.RegionStats;
import live.lbtrip.domain.tourism.repository.TourRegionStatsRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RegionStatsSyncer {

    private final TourApiClient tourApiClient;
    private final TourRegionStatsRepository tourRegionStatsRepository;

    public void sync(RegionCandidate candidate) {
        upsert(candidate, aggregate(tourApiClient.fetchAreaBasedSample(candidate)));
    }

    private RegionStats aggregate(AreaBasedSample sample) {
        Map<Integer, Integer> typeCounts = new HashMap<>();
        Map<CategoryGroup, Integer> groupCounts = new EnumMap<>(CategoryGroup.class);
        for (AreaBasedItem item : sample.items()) {
            int contentTypeId = item.contentTypeId();
            typeCounts.put(contentTypeId, typeCounts.getOrDefault(contentTypeId, 0) + 1);
            for (CategoryGroup group : CategoryGroup.classify(item.cat1(), item.cat2(), item.cat3())) {
                groupCounts.put(group, groupCounts.getOrDefault(group, 0) + 1);
            }
        }
        return RegionStats.of(sample.totalCount(), sample.items().size(), typeCounts, groupCounts);
    }

    private void upsert(RegionCandidate candidate, RegionStats stats) {
        tourRegionStatsRepository
            .findByRegionCandidateId(candidate.getId())
            .ifPresentOrElse(
                regionStats -> {
                    regionStats.update(
                        stats.totalCount(),
                        stats.sampleSize(),
                        stats.typeCounts(),
                        stats.groupCounts()
                    );
                    tourRegionStatsRepository.save(regionStats);
                },
                () -> tourRegionStatsRepository.save(TourRegionStats.create(
                    candidate,
                    stats.totalCount(),
                    stats.sampleSize(),
                    stats.typeCounts(),
                    stats.groupCounts()
                ))
            );
    }
}
