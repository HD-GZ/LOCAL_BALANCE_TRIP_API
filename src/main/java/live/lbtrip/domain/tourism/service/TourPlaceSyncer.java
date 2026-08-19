package live.lbtrip.domain.tourism.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Component;

import live.lbtrip.domain.region.model.RegionCandidate;
import live.lbtrip.domain.tourism.client.TourApiClient;
import live.lbtrip.domain.tourism.client.dto.TourPlaceItem;
import live.lbtrip.domain.tourism.model.entity.TourPlace;
import live.lbtrip.domain.tourism.model.enums.TourContentType;
import live.lbtrip.domain.tourism.repository.TourPlaceRepository;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class TourPlaceSyncer {

    private final TourApiClient tourApiClient;
    private final TourPlaceRepository tourPlaceRepository;

    public List<TourPlaceItem> sync(RegionCandidate candidate, Locale locale) {
        List<TourPlaceItem> fetched = new ArrayList<>();
        for (TourContentType contentType : TourContentType.courseCandidates()) {
            List<TourPlaceItem> places = tourApiClient.fetchPlaces(
                locale,
                candidate.getLdongRegnCd(),
                candidate.getLdongSignguCd(),
                contentType
            );
            for (int order = 0; order < places.size(); order++) {
                upsert(places.get(order), candidate, locale, order);
            }
            fetched.addAll(places);
        }
        return fetched;
    }

    public void syncOverviews(Locale locale) {
        List<TourPlace> pending = tourPlaceRepository.findAllByLocaleAndOverviewIsNull(locale);
        int successCount = 0;
        for (TourPlace place : pending) {
            try {
                String overview = tourApiClient.fetchOverview(locale, place.getContentId());
                place.updateOverview(overview == null ? "" : overview);
                tourPlaceRepository.save(place);
                successCount++;
            } catch (BusinessException e) {
                if (e.getErrorCode() != ErrorCode.TOUR_API_QUOTA_EXCEEDED) {
                    log.warn("overview 적재 실패 - 다음 장소 진행: locale={}, contentId={}", locale, place.getContentId(), e);
                    continue;
                }
                log.warn("overview 적재 중단 - 일일 한도 초과: locale={}, success={}/{}", locale, successCount, pending.size());
                return;
            } catch (Exception e) {
                log.warn("overview 적재 실패 - 다음 장소 진행: locale={}, contentId={}", locale, place.getContentId(), e);
            }
        }
        log.info("overview 적재 완료: locale={}, success={}/{}", locale, successCount, pending.size());
    }

    private void upsert(TourPlaceItem item, RegionCandidate candidate, Locale locale, int sortOrder) {
        tourPlaceRepository.findByLocaleAndContentId(locale, item.contentId())
            .ifPresentOrElse(
                tourPlace -> {
                    tourPlace.update(
                        item.title(),
                        item.imageUrl(),
                        item.longitude(),
                        item.latitude(),
                        sortOrder
                    );
                    tourPlaceRepository.save(tourPlace);
                },
                () -> tourPlaceRepository.save(TourPlace.create(
                    locale,
                    item.contentId(),
                    candidate,
                    item.contentTypeId(),
                    item.title(),
                    item.imageUrl(),
                    item.longitude(),
                    item.latitude(),
                    sortOrder)
                )
            );
    }
}
