package live.lbtrip.domain.tourism.service;

import java.util.List;

import org.springframework.stereotype.Component;

import live.lbtrip.domain.region.repository.RegionCandidateRepository;
import live.lbtrip.domain.tourism.client.dto.RegionStats;
import live.lbtrip.domain.tourism.repository.TourRegionStatsRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RegionStatsFinder {

    private final RegionCandidateRepository regionCandidateRepository;
    private final TourRegionStatsRepository tourRegionStatsRepository;

    public List<RegionStats> findAll() {
        return regionCandidateRepository.findAll().stream()
            .map(candidate -> tourRegionStatsRepository
                .findByRegionCandidateId(candidate.getId())
                .map(stats -> RegionStats.of(stats, candidate)))
            .flatMap(java.util.Optional::stream)
            .toList();
    }
}
