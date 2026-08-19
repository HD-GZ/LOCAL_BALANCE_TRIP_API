package live.lbtrip.domain.tourism.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import live.lbtrip.domain.region.model.RegionCandidate;
import live.lbtrip.domain.tourism.client.TourApiClient;
import live.lbtrip.domain.tourism.client.dto.EnglishPlaceItem;
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
public class EnglishPlaceSyncer {

    private final TourApiClient tourApiClient;
    private final TourPlaceRepository tourPlaceRepository;
    private final EnglishPlaceMatcher englishPlaceMatcher;

    public void sync(RegionCandidate candidate) {
        Map<Integer, List<TourPlace>> placesByType = tourPlaceRepository
            .findAllByRegionCandidateIdOrderByContentTypeIdAscSortOrderAsc(candidate.getId())
            .stream()
            .collect(Collectors.groupingBy(TourPlace::getContentTypeId));

        int matchedCount = 0;
        int fetchedCount = 0;
        for (TourContentType contentType : TourContentType.courseCandidates()) {
            List<EnglishPlaceItem> items = tourApiClient.fetchEnglishPlaces(
                candidate.getLdongRegnCd(),
                candidate.getLdongSignguCd(),
                contentType.getEngCode()
            );
            List<TourPlace> candidates = placesByType.getOrDefault(contentType.getCode(), List.of());
            fetchedCount += items.size();
            for (EnglishPlaceItem item : items) {
                matchedCount += applyMatch(item, candidates) ? 1 : 0;
            }
        }
        log.info("영문 장소 적재 완료: region={}, matched={}/{}", candidate.getName(), matchedCount, fetchedCount);
    }

    public void syncOverviews() {
        List<TourPlace> pending = tourPlaceRepository.findAllByEngContentIdIsNotNullAndOverviewEnIsNull();
        int successCount = 0;
        for (TourPlace place : pending) {
            try {
                String overview = tourApiClient.fetchEnglishOverview(place.getEngContentId());
                place.updateEnglishOverview(overview == null ? "" : overview);
                tourPlaceRepository.save(place);
                successCount++;
            } catch (BusinessException e) {
                if (e.getErrorCode() != ErrorCode.TOUR_API_QUOTA_EXCEEDED) {
                    log.warn("영문 overview 적재 실패 - 다음 장소 진행: engContentId={}", place.getEngContentId(), e);
                    continue;
                }
                log.warn("영문 overview 적재 중단 - 일일 한도 초과: success={}/{}", successCount, pending.size());
                return;
            } catch (Exception e) {
                log.warn("영문 overview 적재 실패 - 다음 장소 진행: engContentId={}", place.getEngContentId(), e);
            }
        }
        log.info("영문 overview 적재 완료: success={}/{}", successCount, pending.size());
    }

    private boolean applyMatch(EnglishPlaceItem item, List<TourPlace> candidates) {
        return englishPlaceMatcher.match(item, candidates)
            .map(place -> {
                place.updateEnglish(item.contentId(), item.title());
                tourPlaceRepository.save(place);
                return true;
            })
            .orElseGet(() -> {
                log.debug("영문 장소 매칭 실패: engContentId={}, title={}", item.contentId(), item.title());
                return false;
            });
    }
}
