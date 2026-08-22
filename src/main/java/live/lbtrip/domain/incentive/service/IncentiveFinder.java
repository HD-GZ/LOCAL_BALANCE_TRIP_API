package live.lbtrip.domain.incentive.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Component;

import live.lbtrip.domain.incentive.model.Incentive;
import live.lbtrip.domain.incentive.model.vo.LocalizedIncentive;
import live.lbtrip.domain.incentive.repository.IncentiveRepository;
import live.lbtrip.global.i18n.MessageResolver;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class IncentiveFinder {

    private final IncentiveRepository incentiveRepository;
    private final MessageResolver messageResolver;

    public List<LocalizedIncentive> findAllByRegion(Long regionCandidateId) {
        if (regionCandidateId == null) {
            return List.of();
        }
        return localize(incentiveRepository.findAllByRegion(regionCandidateId));
    }

    public List<LocalizedIncentive> findActiveByRegion(Long regionCandidateId, LocalDate today) {
        if (regionCandidateId == null) {
            return List.of();
        }
        return localize(incentiveRepository.findActiveByRegion(regionCandidateId, today));
    }

    private List<LocalizedIncentive> localize(List<Incentive> incentives) {
        Locale locale = messageResolver.currentLocale();
        return incentives.stream()
            .map(incentive -> LocalizedIncentive.of(incentive, locale))
            .toList();
    }
}
