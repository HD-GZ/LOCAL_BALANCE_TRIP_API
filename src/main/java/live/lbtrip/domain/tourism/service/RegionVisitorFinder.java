package live.lbtrip.domain.tourism.service;

import org.springframework.stereotype.Component;

import live.lbtrip.domain.tourism.model.entity.RegionVisitorStats;
import live.lbtrip.domain.tourism.model.enums.VisitorType;
import live.lbtrip.domain.tourism.repository.RegionVisitorStatsRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RegionVisitorFinder {

    private static final int RECENT_DAYS = 30;

    private final RegionVisitorStatsRepository regionVisitorStatsRepository;

    public double sumRecentOutsiderVisitors(String ldongRegnCd, String ldongSignguCd) {
        return regionVisitorStatsRepository.findFirstByOrderByBaseDateDesc()
            .map(latest -> regionVisitorStatsRepository
                .findAllByLdongRegnCdAndLdongSignguCdAndVisitorTypeAndBaseDateAfter(
                    ldongRegnCd,
                    ldongSignguCd,
                    VisitorType.OUTSIDER,
                    latest.getBaseDate().minusDays(RECENT_DAYS)
                )
                .stream()
                .mapToDouble(RegionVisitorStats::getVisitorCount)
                .sum())
            .orElse(0.0);
    }
}
