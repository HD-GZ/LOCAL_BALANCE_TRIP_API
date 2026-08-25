package live.lbtrip.domain.tourism.service;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import live.lbtrip.domain.region.model.RegionCandidate;
import live.lbtrip.domain.tourism.client.TourApiClient;
import live.lbtrip.domain.tourism.client.dto.TourEventItem;
import live.lbtrip.domain.tourism.model.entity.TourEvent;
import live.lbtrip.domain.tourism.repository.TourEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class TourEventSyncer {

    public static final int WINDOW_DAYS = 60;

    private final TourApiClient tourApiClient;
    private final TourEventRepository tourEventRepository;

    @Transactional
    public void sync(RegionCandidate candidate, Locale locale) {
        LocalDate today = LocalDate.now();
        List<TourEventItem> fetched = tourApiClient.fetchFestivals(
            candidate.getLdongRegnCd(), candidate.getLdongSignguCd(), today, today.plusDays(WINDOW_DAYS), locale);

        Set<String> activeContentIds = new HashSet<>();
        for (TourEventItem item : fetched) {
            if (item.eventEnd().isBefore(today)) {
                continue;
            }
            upsert(item, candidate, locale);
            activeContentIds.add(item.contentId());
        }

        List<TourEvent> stale = tourEventRepository
            .findAllByLocaleAndRegionCandidateId(locale, candidate.getId()).stream()
            .filter(event -> !activeContentIds.contains(event.getContentId()) || event.getEventEnd().isBefore(today))
            .toList();
        if (!stale.isEmpty()) {
            tourEventRepository.deleteAll(stale);
        }
        log.info("행사 적재 완료: locale={}, region={}, active={}, removed={}",
            locale, candidate.getName(), activeContentIds.size(), stale.size());
    }

    private void upsert(TourEventItem item, RegionCandidate candidate, Locale locale) {
        tourEventRepository.findByLocaleAndContentId(locale, item.contentId())
            .ifPresentOrElse(
                event -> {
                    event.update(item.title(), item.imageUrl(), item.latitude(), item.longitude(),
                        item.address(), item.eventStart(), item.eventEnd());
                    tourEventRepository.save(event);
                },
                () -> tourEventRepository.save(TourEvent.create(
                    locale, item.contentId(), candidate, item.title(), item.imageUrl(),
                    item.latitude(), item.longitude(), item.address(), item.eventStart(), item.eventEnd()))
            );
    }
}
