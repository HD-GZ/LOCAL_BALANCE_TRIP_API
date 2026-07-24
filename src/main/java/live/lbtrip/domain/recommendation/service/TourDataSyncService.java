package live.lbtrip.domain.recommendation.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import live.lbtrip.domain.recommendation.client.OdiiClient;
import live.lbtrip.domain.recommendation.client.TourApiClient;
import live.lbtrip.domain.recommendation.client.dto.OdiiThemeItem;
import live.lbtrip.domain.recommendation.client.dto.RegionStats;
import live.lbtrip.domain.recommendation.client.dto.TourPlaceItem;
import live.lbtrip.domain.recommendation.model.entity.OdiiTheme;
import live.lbtrip.domain.recommendation.model.entity.TourPlace;
import live.lbtrip.domain.recommendation.model.entity.TourRegionStats;
import live.lbtrip.domain.recommendation.model.enums.TourContentType;
import live.lbtrip.domain.recommendation.repository.OdiiThemeRepository;
import live.lbtrip.domain.recommendation.repository.TourPlaceRepository;
import live.lbtrip.domain.recommendation.repository.TourRegionStatsRepository;
import live.lbtrip.domain.region.model.RegionCandidate;
import live.lbtrip.domain.region.repository.RegionCandidateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TourDataSyncService {

    private final RegionCandidateRepository regionCandidateRepository;
    private final TourApiClient tourApiClient;
    private final OdiiClient odiiClient;
    private final TourRegionStatsRepository tourRegionStatsRepository;
    private final TourPlaceRepository tourPlaceRepository;
    private final OdiiThemeRepository odiiThemeRepository;

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
        log.info("관광 데이터 적재 완료: successRegions={}/{}, elapsedMs={}",
            successCount, candidates.size(), elapsedMillis(startedAt));
    }

    private void syncRegion(RegionCandidate candidate) {
        long startedAt = System.nanoTime();
        RegionStats stats = tourApiClient.fetchRegionStats(candidate);
        upsertStats(stats);

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
        log.info("지역 데이터 적재 성공: region={}, placeCount={}, elapsedMs={}",
            candidate.getName(), fetchedPlaces.size(), elapsedMillis(startedAt));
    }

    private void upsertStats(RegionStats stats) {
        tourRegionStatsRepository
            .findByLdongRegnCdAndLdongSignguCd(stats.ldongRegnCd(), stats.ldongSignguCd())
            .ifPresentOrElse(
                existing -> {
                    existing.update(stats.totalCount(), stats.sampleSize(), stats.typeCounts());
                    tourRegionStatsRepository.save(existing);
                },
                () -> tourRegionStatsRepository.save(TourRegionStats.create(
                    stats.ldongRegnCd(), stats.ldongSignguCd(),
                    stats.totalCount(), stats.sampleSize(), stats.typeCounts())));
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
                    item.contentId(), candidate.getLdongRegnCd(), candidate.getLdongSignguCd(),
                    item.contentTypeId(), item.title(), item.imageUrl(),
                    item.longitude(), item.latitude(), sortOrder)));
    }

    private void upsertThemes(List<TourPlaceItem> places) {
        if (places.isEmpty()) {
            return;
        }
        List<OdiiThemeItem> themes = odiiClient.fetchThemesNear(
            averageLongitude(places), averageLatitude(places));
        for (OdiiThemeItem item : themes) {
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

    private double averageLongitude(List<TourPlaceItem> places) {
        return places.stream().filter(place -> place.longitude() != null)
            .mapToDouble(TourPlaceItem::longitude).average().orElse(0);
    }

    private double averageLatitude(List<TourPlaceItem> places) {
        return places.stream().filter(place -> place.latitude() != null)
            .mapToDouble(TourPlaceItem::latitude).average().orElse(0);
    }

    private long elapsedMillis(long startedAt) {
        return (System.nanoTime() - startedAt) / 1_000_000;
    }
}
