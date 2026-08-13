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
import live.lbtrip.domain.tourism.model.vo.CategoryGroupMapping;
import live.lbtrip.domain.tourism.model.vo.RegionStats;
import live.lbtrip.domain.tourism.repository.TourRegionStatsRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RegionStatsSyncer {

    private final TourApiClient tourApiClient;
    private final CategoryGroupClassifier categoryGroupClassifier;
    private final TourRegionStatsRepository tourRegionStatsRepository;

    public void sync(RegionCandidate candidate) {
        upsert(candidate, aggregate(tourApiClient.fetchAreaBasedSample(candidate)));
    }

    private RegionStats aggregate(AreaBasedSample sample) {
        CategoryGroupMapping mapping = categoryGroupClassifier.load();
        Map<Integer, Integer> typeCounts = new HashMap<>();
        Map<CategoryGroup, Integer> groupCounts = new EnumMap<>(CategoryGroup.class);
        for (AreaBasedItem item : sample.items()) {
            typeCounts.merge(item.contentTypeId(), 1, Integer::sum);
            for (CategoryGroup group : mapping.classify(item.cat1(), item.cat2(), item.cat3())) {
                groupCounts.merge(group, 1, Integer::sum);
            }
        }
        return RegionStats.of(sample.totalCount(), sample.items().size(), typeCounts, groupCounts);
    }

    private void upsert(RegionCandidate candidate, RegionStats stats) {
        tourRegionStatsRepository
            .findByRegionCandidateId(candidate.getId())
            .ifPresentOrElse(
                existing -> {
                    existing.update(stats.totalCount(), stats.sampleSize(),
                        stats.typeCounts(), stats.groupCounts());
                    tourRegionStatsRepository.save(existing);
                },
                () -> tourRegionStatsRepository.save(TourRegionStats.create(
                    candidate, stats.totalCount(), stats.sampleSize(),
                    stats.typeCounts(), stats.groupCounts())));
    }
}
