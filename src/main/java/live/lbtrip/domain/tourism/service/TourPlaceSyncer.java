package live.lbtrip.domain.tourism.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import live.lbtrip.domain.region.model.RegionCandidate;
import live.lbtrip.domain.tourism.client.TourApiClient;
import live.lbtrip.domain.tourism.client.dto.TourPlaceItem;
import live.lbtrip.domain.tourism.model.entity.TourPlace;
import live.lbtrip.domain.tourism.model.enums.TourContentType;
import live.lbtrip.domain.tourism.repository.TourPlaceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class TourPlaceSyncer {

    private final TourApiClient tourApiClient;
    private final TourPlaceRepository tourPlaceRepository;

    public List<TourPlaceItem> sync(RegionCandidate candidate) {
        List<TourPlaceItem> fetched = new ArrayList<>();
        for (TourContentType contentType : TourContentType.courseCandidates()) {
            List<TourPlaceItem> places = tourApiClient.fetchPlaces(
                candidate.getLdongRegnCd(), candidate.getLdongSignguCd(), contentType.getCode());
            for (int order = 0; order < places.size(); order++) {
                upsert(places.get(order), candidate, order);
            }
            fetched.addAll(places);
        }
        return fetched;
    }

    public void syncOverviews() {
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

    private void upsert(TourPlaceItem item, RegionCandidate candidate, int sortOrder) {
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
}
