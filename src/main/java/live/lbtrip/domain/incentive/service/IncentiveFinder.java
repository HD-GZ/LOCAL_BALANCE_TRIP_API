package live.lbtrip.domain.incentive.service;

import java.util.List;

import org.springframework.stereotype.Component;

import live.lbtrip.domain.incentive.model.Incentive;
import live.lbtrip.domain.incentive.repository.IncentiveRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class IncentiveFinder {

    private final IncentiveRepository incentiveRepository;

    public List<Incentive> findAllByRegion(String ldongRegnCd,  String ldongSignguCd) {
        if (ldongRegnCd == null || ldongSignguCd == null) {
            return List.of();
        }
        return incentiveRepository.findAllByRegion(ldongRegnCd, ldongSignguCd);
    }
}
