package live.lbtrip.domain.tourism.service;

import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;

import live.lbtrip.domain.region.model.RegionCandidate;
import live.lbtrip.domain.region.repository.RegionCandidateRepository;
import live.lbtrip.domain.tourism.client.dto.TourPlaceItem;
import live.lbtrip.domain.tourism.model.enums.TourSyncStep;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import live.lbtrip.global.i18n.LocaleConfig;
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
    private final RegionNameSyncer regionNameSyncer;
    private final TrailCourseSyncer trailCourseSyncer;

    public void syncAll() {
        long startedAt = System.nanoTime();
        for (TourSyncStep step : TourSyncStep.values()) {
            sync(step);
        }
        log.info("관광 데이터 적재 완료: elapsedMs={}", elapsedMillis(startedAt));
    }

    public void sync(TourSyncStep step) {
        long startedAt = System.nanoTime();
        switch (step) {
            case REGIONS -> syncRegions(regionCandidateRepository.findAll());
            case PLACE_THEMES -> linkPlaceThemes(regionCandidateRepository.findAll());
            case OVERVIEWS -> tourPlaceSyncer.syncOverviews(LocaleConfig.DEFAULT_LOCALE);
            case AUDIO_URLS -> odiiThemeSyncer.syncAudioUrls();
            case VISITOR_STATS -> visitorStatsSyncer.sync();
            case PLACES_EN -> syncPlaces(regionCandidateRepository.findAll(), Locale.ENGLISH);
            case OVERVIEWS_EN -> tourPlaceSyncer.syncOverviews(Locale.ENGLISH);
            case REGION_NAMES_EN -> regionNameSyncer.syncEnglishNames();
            case TRAIL_COURSES -> trailCourseSyncer.sync();
        }
        log.info("관광 데이터 적재 단계 종료: step={}, elapsedMs={}", step, elapsedMillis(startedAt));
    }

    private void syncRegions(List<RegionCandidate> candidates) {
        int successCount = 0;
        for (RegionCandidate candidate : candidates) {
            try {
                syncRegion(candidate);
                successCount++;
            } catch (BusinessException e) {
                if (e.getErrorCode() != ErrorCode.TOUR_API_QUOTA_EXCEEDED) {
                    log.error("지역 데이터 적재 실패 - 다음 지역 진행: region={}", candidate.getName(), e);
                    continue;
                }
                log.warn("지역 데이터 적재 중단 - 일일 한도 초과: success={}/{}",
                    successCount, candidates.size());
                return;
            } catch (Exception e) {
                log.error("지역 데이터 적재 실패 - 다음 지역 진행: region={}", candidate.getName(), e);
            }
        }
        log.info("지역 데이터 적재 완료: success={}/{}", successCount, candidates.size());
    }

    private void syncRegion(RegionCandidate candidate) {
        long startedAt = System.nanoTime();
        regionStatsSyncer.sync(candidate);
        List<TourPlaceItem> places = tourPlaceSyncer.sync(candidate, LocaleConfig.DEFAULT_LOCALE);
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

    private void syncPlaces(List<RegionCandidate> candidates, Locale locale) {
        int successCount = 0;
        for (RegionCandidate candidate : candidates) {
            try {
                tourPlaceSyncer.sync(candidate, locale);
                successCount++;
            } catch (BusinessException e) {
                if (e.getErrorCode() != ErrorCode.TOUR_API_QUOTA_EXCEEDED) {
                    log.error("장소 적재 실패 - 다음 지역 진행: locale={}, region={}", locale, candidate.getName(), e);
                    continue;
                }
                log.warn("장소 적재 중단 - 일일 한도 초과: locale={}, success={}/{}", locale, successCount, candidates.size());
                return;
            } catch (Exception e) {
                log.error("장소 적재 실패 - 다음 지역 진행: locale={}, region={}", locale, candidate.getName(), e);
            }
        }
        log.info("장소 적재 완료: locale={}, success={}/{}", locale, successCount, candidates.size());
    }

    private long elapsedMillis(long startedAt) {
        return (System.nanoTime() - startedAt) / 1_000_000;
    }
}
