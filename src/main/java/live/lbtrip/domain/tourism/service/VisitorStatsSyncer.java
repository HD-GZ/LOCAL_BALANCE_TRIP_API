package live.lbtrip.domain.tourism.service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import live.lbtrip.domain.region.model.RegionCandidate;
import live.lbtrip.domain.region.repository.RegionCandidateRepository;
import live.lbtrip.domain.tourism.client.DataLabClient;
import live.lbtrip.domain.tourism.client.dto.VisitorStatItem;
import live.lbtrip.domain.tourism.model.entity.RegionVisitorStats;
import live.lbtrip.domain.tourism.repository.RegionVisitorStatsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class VisitorStatsSyncer {

    private static final int LOOKBACK_DAYS = 45;

    private final RegionCandidateRepository regionCandidateRepository;
    private final DataLabClient dataLabClient;
    private final RegionVisitorStatsRepository regionVisitorStatsRepository;

    public void sync() {
        Map<String, RegionCandidate> candidatesByCode = candidatesByCode();
        int syncedDays = 0;
        for (int daysAgo = LOOKBACK_DAYS; daysAgo >= 1; daysAgo--) {
            if (syncDate(LocalDate.now().minusDays(daysAgo), candidatesByCode)) {
                syncedDays++;
            }
        }
        log.info("방문자수 적재 완료: syncedDays={}", syncedDays);
    }

    private boolean syncDate(LocalDate baseDate, Map<String, RegionCandidate> candidatesByCode) {
        if (regionVisitorStatsRepository.existsByBaseDate(baseDate)) {
            return false;
        }
        List<VisitorStatItem> items = dataLabClient.fetchDailyVisitors(baseDate);
        if (items.isEmpty()) {
            return false;
        }
        for (VisitorStatItem item : items) {
            RegionCandidate candidate = candidatesByCode.get(item.signguCode());
            if (candidate != null) {
                upsert(candidate, item);
            }
        }
        return true;
    }

    private Map<String, RegionCandidate> candidatesByCode() {
        Map<String, RegionCandidate> candidatesByCode = new HashMap<>();
        for (RegionCandidate candidate : regionCandidateRepository.findAll()) {
            candidatesByCode.put(candidate.getLdongRegnCd() + candidate.getLdongSignguCd(), candidate);
        }
        return candidatesByCode;
    }

    private void upsert(RegionCandidate candidate, VisitorStatItem item) {
        regionVisitorStatsRepository
            .findByRegionCandidateIdAndBaseDateAndVisitorType(
                candidate.getId(), item.baseDate(), item.visitorType())
            .ifPresentOrElse(
                existing -> {
                    existing.updateCount(item.visitorCount());
                    regionVisitorStatsRepository.save(existing);
                },
                () -> regionVisitorStatsRepository.save(RegionVisitorStats.create(
                    candidate, item.baseDate(), item.visitorType(), item.visitorCount())));
    }
}
