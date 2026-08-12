package live.lbtrip.domain.incentive.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Component;

import live.lbtrip.domain.incentive.model.Incentive;
import live.lbtrip.domain.incentive.repository.IncentiveRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class IncentiveFinder {

    private final IncentiveRepository incentiveRepository;

    public List<Incentive> findAllByRegion(Long regionCandidateId) {
        if (regionCandidateId == null) {
            return List.of();
        }
        return incentiveRepository.findAllByRegion(regionCandidateId);
    }

    public List<Incentive> findActiveByRegion(Long regionCandidateId, LocalDate today) {
        if (regionCandidateId == null) {
            return List.of();
        }
        return incentiveRepository.findActiveByRegion(regionCandidateId, today);
    }
}
