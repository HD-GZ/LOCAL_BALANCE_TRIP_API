package live.lbtrip.domain.tourism.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import live.lbtrip.domain.region.model.RegionGreenMetrics;
import live.lbtrip.domain.region.repository.RegionGreenMetricsRepository;
import live.lbtrip.domain.tourism.model.entity.TourRegionStats;
import live.lbtrip.domain.tourism.model.enums.VisitorType;
import live.lbtrip.domain.tourism.model.vo.RegionMetrics;
import live.lbtrip.domain.tourism.repository.RegionVisitorStatsRepository;
import live.lbtrip.domain.tourism.repository.TourRegionStatsRepository;
import live.lbtrip.domain.tourism.repository.dto.RegionVisitorSum;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RegionMetricsFinder {

    private static final int RECENT_DAYS = 30;

    private final TourRegionStatsRepository tourRegionStatsRepository;
    private final RegionVisitorStatsRepository regionVisitorStatsRepository;
    private final RegionGreenMetricsRepository regionGreenMetricsRepository;

    public List<RegionMetrics> findAllMetrics() {
        List<TourRegionStats> statsList = tourRegionStatsRepository.findAllWithRegionCandidate();
        if (statsList.isEmpty()) {
            throw BusinessException.of(ErrorCode.TOUR_DATA_NOT_READY);
        }

        Map<Long, Double> visitorSums = recentOutsiderVisitorSums();
        Map<Long, Integer> greenScores = greenScores();
        return statsList.stream()
            .map(stats -> RegionMetrics.of(
                stats,
                stats.getRegionCandidate(),
                visitorSums.getOrDefault(stats.getRegionCandidate().getId(), 0.0),
                greenScores.getOrDefault(stats.getRegionCandidate().getId(), 0)))
            .toList();
    }

    private Map<Long, Integer> greenScores() {
        return regionGreenMetricsRepository.findAllWithRegionCandidate().stream()
            .collect(Collectors.toMap(
                metrics -> metrics.getRegionCandidate().getId(),
                RegionGreenMetrics::greenScore));
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
