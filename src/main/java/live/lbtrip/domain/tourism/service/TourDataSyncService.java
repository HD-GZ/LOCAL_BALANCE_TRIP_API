package live.lbtrip.domain.tourism.service;

import java.util.List;

import org.springframework.stereotype.Service;

import live.lbtrip.domain.region.model.RegionCandidate;
import live.lbtrip.domain.region.repository.RegionCandidateRepository;
import live.lbtrip.domain.tourism.client.dto.TourPlaceItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TourDataSyncService {

    private final RegionCandidateRepository regionCandidateRepository;
    private final RegionStatsSyncer regionStatsSyncer;
    private final TourPlaceSyncer tourPlaceSyncer;
    private final OdiiThemeSyncer odiiThemeSyncer;
    private final PlaceThemeLinker placeThemeLinker;
    private final VisitorStatsSyncer visitorStatsSyncer;

    public void syncAll() {
        long startedAt = System.nanoTime();
        List<RegionCandidate> candidates = regionCandidateRepository.findAll();
        int successCount = 0;
        for (RegionCandidate candidate : candidates) {
            try {
                syncRegion(candidate);
                successCount++;
            } catch (Exception e) {
                log.error("지역 데이터 적재 실패 - 다음 지역 진행: region={}", candidate.getName(), e);
            }
        }
        linkPlaceThemes(candidates);
        tourPlaceSyncer.syncOverviews();
        odiiThemeSyncer.syncAudioUrls();
        visitorStatsSyncer.sync();
        log.info("관광 데이터 적재 완료: successRegions={}/{}, elapsedMs={}",
            successCount, candidates.size(), elapsedMillis(startedAt));
    }

    private void syncRegion(RegionCandidate candidate) {
        long startedAt = System.nanoTime();
        regionStatsSyncer.sync(candidate);
        List<TourPlaceItem> places = tourPlaceSyncer.sync(candidate);
        odiiThemeSyncer.sync(places);
        log.info("지역 데이터 적재 성공: region={}, placeCount={}, elapsedMs={}",
            candidate.getName(), places.size(), elapsedMillis(startedAt));
    }

    private void linkPlaceThemes(List<RegionCandidate> candidates) {
        int successCount = 0;
        for (RegionCandidate candidate : candidates) {
            try {
                placeThemeLinker.link(candidate);
                successCount++;
            } catch (Exception e) {
                log.error("장소-테마 매칭 실패 - 다음 지역 진행: region={}", candidate.getName(), e);
            }
        }
        log.info("장소-테마 매칭 완료: success={}/{}", successCount, candidates.size());
    }

    private long elapsedMillis(long startedAt) {
        return (System.nanoTime() - startedAt) / 1_000_000;
    }
}
