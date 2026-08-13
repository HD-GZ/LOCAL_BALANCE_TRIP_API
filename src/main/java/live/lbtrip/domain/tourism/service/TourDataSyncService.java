package live.lbtrip.domain.tourism.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import live.lbtrip.domain.region.model.RegionCandidate;
import live.lbtrip.domain.region.repository.RegionCandidateRepository;
import live.lbtrip.domain.tourism.client.DataLabClient;
import live.lbtrip.domain.tourism.client.OdiiClient;
import live.lbtrip.domain.tourism.client.TourApiClient;
import live.lbtrip.domain.tourism.client.dto.OdiiThemeItem;
import live.lbtrip.domain.tourism.client.dto.RegionStats;
import live.lbtrip.domain.tourism.client.dto.TourPlaceItem;
import live.lbtrip.domain.tourism.client.dto.VisitorStatItem;
import live.lbtrip.domain.tourism.model.entity.OdiiTheme;
import live.lbtrip.domain.tourism.model.entity.RegionVisitorStats;
import live.lbtrip.domain.tourism.model.entity.TourPlace;
import live.lbtrip.domain.tourism.model.entity.TourRegionStats;
import live.lbtrip.domain.tourism.model.enums.TourContentType;
import live.lbtrip.domain.tourism.model.vo.Centroid;
import live.lbtrip.domain.tourism.repository.OdiiThemeRepository;
import live.lbtrip.domain.tourism.repository.RegionVisitorStatsRepository;
import live.lbtrip.domain.tourism.repository.TourPlaceRepository;
import live.lbtrip.domain.tourism.repository.TourRegionStatsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TourDataSyncService {

    private static final int VISITOR_LOOKBACK_DAYS = 45;
    private static final double THEME_LOOKUP_LON_DELTA = 0.23;
    private static final double THEME_LOOKUP_LAT_DELTA = 0.18;

    private final RegionCandidateRepository regionCandidateRepository;
    private final TourApiClient tourApiClient;
    private final OdiiClient odiiClient;
    private final DataLabClient dataLabClient;
    private final TourRegionStatsRepository tourRegionStatsRepository;
    private final TourPlaceRepository tourPlaceRepository;
    private final OdiiThemeRepository odiiThemeRepository;
    private final RegionVisitorStatsRepository regionVisitorStatsRepository;
    private final OdiiThemeMatcher odiiThemeMatcher;

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
        syncOverviews();
        syncAudioUrls();
        syncVisitorStats();
        log.info("관광 데이터 적재 완료: successRegions={}/{}, elapsedMs={}",
            successCount, candidates.size(), elapsedMillis(startedAt));
    }

    private void syncRegion(RegionCandidate candidate) {
        long startedAt = System.nanoTime();
        RegionStats stats = tourApiClient.fetchRegionStats(candidate);
        upsertStats(candidate, stats);

        List<TourPlaceItem> fetchedPlaces = new ArrayList<>();
        for (TourContentType contentType : TourContentType.courseCandidates()) {
            List<TourPlaceItem> places = tourApiClient.fetchPlaces(
                candidate.getLdongRegnCd(), candidate.getLdongSignguCd(), contentType.getCode());
            for (int order = 0; order < places.size(); order++) {
                upsertPlace(places.get(order), candidate, order);
            }
            fetchedPlaces.addAll(places);
        }
        upsertThemes(fetchedPlaces);
        matchPlaceThemes(candidate);
        log.info("지역 데이터 적재 성공: region={}, placeCount={}, elapsedMs={}",
            candidate.getName(), fetchedPlaces.size(), elapsedMillis(startedAt));
    }

    private void matchPlaceThemes(RegionCandidate candidate) {
        List<TourPlace> places = tourPlaceRepository
            .findAllByRegionCandidateIdOrderByContentTypeIdAscSortOrderAsc(candidate.getId());
        Centroid.of(places, TourPlace::getLongitude, TourPlace::getLatitude)
            .ifPresent(centroid -> assignThemes(places, findThemesNear(centroid)));
    }

    private List<OdiiTheme> findThemesNear(Centroid centroid) {
        return odiiThemeRepository.findAllByLongitudeBetweenAndLatitudeBetween(
            centroid.longitude() - THEME_LOOKUP_LON_DELTA,
            centroid.longitude() + THEME_LOOKUP_LON_DELTA,
            centroid.latitude() - THEME_LOOKUP_LAT_DELTA,
            centroid.latitude() + THEME_LOOKUP_LAT_DELTA);
    }

    private void assignThemes(List<TourPlace> places, List<OdiiTheme> themes) {
        for (TourPlace place : places) {
            place.assignOdiiTheme(odiiThemeMatcher.match(place, themes).orElse(null));
            tourPlaceRepository.save(place);
        }
    }

    private void upsertStats(RegionCandidate candidate, RegionStats stats) {
        tourRegionStatsRepository
            .findByRegionCandidateId(candidate.getId())
            .ifPresentOrElse(
                existing -> {
                    existing.update(stats.totalCount(), stats.sampleSize(),
                        stats.typeCounts(), stats.groupCounts());
                    tourRegionStatsRepository.save(existing);
                },
                () -> tourRegionStatsRepository.save(TourRegionStats.create(
                    candidate, stats.totalCount(), stats.sampleSize(),
                    stats.typeCounts(), stats.groupCounts())));
    }

    private void upsertPlace(TourPlaceItem item, RegionCandidate candidate, int sortOrder) {
        tourPlaceRepository.findByContentId(item.contentId())
            .ifPresentOrElse(
                existing -> {
                    existing.update(item.title(), item.imageUrl(),
                        item.longitude(), item.latitude(), sortOrder);
                    tourPlaceRepository.save(existing);
                },
                () -> tourPlaceRepository.save(TourPlace.create(
                    item.contentId(), candidate, item.contentTypeId(), item.title(), item.imageUrl(),
                    item.longitude(), item.latitude(), sortOrder)));
    }

    private void upsertThemes(List<TourPlaceItem> places) {
        Centroid.of(places, TourPlaceItem::longitude, TourPlaceItem::latitude)
            .ifPresent(this::upsertThemesNear);
    }

    private void upsertThemesNear(Centroid centroid) {
        for (OdiiThemeItem item : odiiClient.fetchThemesNear(centroid.longitude(), centroid.latitude())) {
            odiiThemeRepository.findByTidAndTlid(item.tid(), item.tlid())
                .ifPresentOrElse(
                    existing -> {
                        existing.update(item.title(), item.longitude(), item.latitude());
                        odiiThemeRepository.save(existing);
                    },
                    () -> odiiThemeRepository.save(OdiiTheme.create(
                        item.tid(), item.tlid(), item.title(), item.longitude(), item.latitude())));
        }
    }

    private void syncOverviews() {
        List<TourPlace> pending = tourPlaceRepository.findAllByOverviewIsNull();
        int successCount = 0;
        for (TourPlace place : pending) {
            try {
                String overview = tourApiClient.fetchOverview(place.getContentId());
                place.updateOverview(overview == null ? "" : overview);
                tourPlaceRepository.save(place);
                successCount++;
            } catch (Exception e) {
                log.warn("overview 적재 실패 - 다음 장소 진행: contentId={}", place.getContentId(), e);
            }
        }
        log.info("overview 적재 완료: success={}/{}", successCount, pending.size());
    }

    private void syncAudioUrls() {
        List<OdiiTheme> pending = odiiThemeRepository.findAllByAudioSyncedAtIsNull();
        for (OdiiTheme theme : pending) {
            String audioUrl = odiiClient.fetchFirstAudioUrl(theme.getTid(), theme.getTlid());
            theme.updateAudio(audioUrl, LocalDateTime.now());
            odiiThemeRepository.save(theme);
        }
        log.info("Odii 오디오 적재 완료: count={}", pending.size());
    }

    private void syncVisitorStats() {
        Map<String, RegionCandidate> candidatesByCode = new HashMap<>();
        for (RegionCandidate candidate : regionCandidateRepository.findAll()) {
            candidatesByCode.put(candidate.getLdongRegnCd() + candidate.getLdongSignguCd(), candidate);
        }
        int syncedDays = 0;
        for (int daysAgo = VISITOR_LOOKBACK_DAYS; daysAgo >= 1; daysAgo--) {
            LocalDate baseDate = LocalDate.now().minusDays(daysAgo);
            if (regionVisitorStatsRepository.existsByBaseDate(baseDate)) {
                continue;
            }
            List<VisitorStatItem> items = dataLabClient.fetchDailyVisitors(baseDate);
            if (items.isEmpty()) {
                continue;
            }
            for (VisitorStatItem item : items) {
                RegionCandidate candidate = candidatesByCode.get(item.signguCode());
                if (candidate == null) {
                    continue;
                }
                upsertVisitorStat(candidate, item);
            }
            syncedDays++;
        }
        log.info("방문자수 적재 완료: syncedDays={}", syncedDays);
    }

    private void upsertVisitorStat(RegionCandidate candidate, VisitorStatItem item) {
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

    private long elapsedMillis(long startedAt) {
        return (System.nanoTime() - startedAt) / 1_000_000;
    }
}
