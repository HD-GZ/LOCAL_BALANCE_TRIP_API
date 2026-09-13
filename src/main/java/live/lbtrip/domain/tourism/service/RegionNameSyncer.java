package live.lbtrip.domain.tourism.service;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;

import org.springframework.stereotype.Component;

import live.lbtrip.domain.region.model.RegionCandidate;
import live.lbtrip.domain.region.repository.RegionCandidateRepository;
import live.lbtrip.domain.tourism.client.TourApiClient;
import live.lbtrip.domain.tourism.client.dto.RegionNameItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class RegionNameSyncer {

    private final TourApiClient tourApiClient;
    private final RegionCandidateRepository regionCandidateRepository;

    public void syncEnglishNames() {
        List<RegionCandidate> candidates = regionCandidateRepository.findAll();
        Map<String, String> namesByCode = new HashMap<>();
        for (String regnCd : new TreeSet<>(candidates.stream().map(RegionCandidate::getLdongRegnCd).toList())) {
            for (RegionNameItem item : tourApiClient.fetchRegionNames(Locale.ENGLISH, regnCd)) {
                namesByCode.put(key(item.ldongRegnCd(), item.ldongSignguCd()), item.fullName());
            }
        }

        int successCount = 0;
        for (RegionCandidate candidate : candidates) {
            String nameEn = namesByCode.get(key(candidate.getLdongRegnCd(), candidate.getLdongSignguCd()));
            if (nameEn == null) {
                log.warn("지역 영문명 없음: region={}", candidate.getName());
                continue;
            }
            candidate.updateNameEn(nameEn);
            regionCandidateRepository.save(candidate);
            successCount++;
        }
        log.info("지역 영문명 적재 완료: success={}/{}", successCount, candidates.size());
    }

    private String key(String regnCd, String signguCd) {
        return regnCd + "-" + signguCd;
    }
}
