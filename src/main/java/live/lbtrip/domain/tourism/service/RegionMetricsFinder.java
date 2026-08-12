package live.lbtrip.domain.tourism.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import live.lbtrip.domain.tourism.model.enums.VisitorType;
import live.lbtrip.domain.tourism.model.vo.RegionMetrics;
import live.lbtrip.domain.tourism.repository.RegionVisitorStatsRepository;
import live.lbtrip.domain.tourism.repository.TourRegionStatsRepository;
import live.lbtrip.domain.tourism.repository.dto.RegionVisitorSum;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RegionMetricsFinder {

    private static final int RECENT_DAYS = 30;

    private final TourRegionStatsRepository tourRegionStatsRepository;
    private final RegionVisitorStatsRepository regionVisitorStatsRepository;

    public List<RegionMetrics> findAllMetrics() {
        Map<Long, Double> visitorSums = recentOutsiderVisitorSums();
        return tourRegionStatsRepository.findAllWithRegionCandidate().stream()
            .map(stats -> RegionMetrics.of(
                stats,
                stats.getRegionCandidate(),
                visitorSums.getOrDefault(stats.getRegionCandidate().getId(), 0.0)))
            .toList();
    }

    private Map<Long, Double> recentOutsiderVisitorSums() {
        return regionVisitorStatsRepository.findFirstByOrderByBaseDateDesc()
            .map(latest -> regionVisitorStatsRepository
                .sumByRegionCandidate(VisitorType.OUTSIDER, latest.getBaseDate().minusDays(RECENT_DAYS))
                .stream()
                .collect(Collectors.toMap(RegionVisitorSum::getRegionCandidateId, RegionVisitorSum::getTotal)))
            .orElse(Map.of());
    }
}
