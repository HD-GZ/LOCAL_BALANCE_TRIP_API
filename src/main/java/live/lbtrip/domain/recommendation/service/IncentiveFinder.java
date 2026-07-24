package live.lbtrip.domain.recommendation.service;

import java.util.List;

import org.springframework.stereotype.Component;

import live.lbtrip.domain.admin.incentive.model.Incentive;
import live.lbtrip.domain.admin.incentive.repository.IncentiveRepository;
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
