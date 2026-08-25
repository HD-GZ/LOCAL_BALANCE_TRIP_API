package live.lbtrip.domain.tourism.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import live.lbtrip.domain.tourism.model.vo.LocalizedTourEvent;
import live.lbtrip.domain.tourism.repository.TourEventRepository;
import live.lbtrip.global.i18n.MessageResolver;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TourEventFinder {

    public static final int LIMIT = 5;

    private final TourEventRepository tourEventRepository;
    private final MessageResolver messageResolver;

    public List<LocalizedTourEvent> findActiveByRegion(Long regionCandidateId, LocalDate today) {
        if (regionCandidateId == null) {
            return List.of();
        }
        return tourEventRepository
            .findActiveByRegion(messageResolver.currentLocale(), regionCandidateId, today, PageRequest.of(0, LIMIT))
            .stream()
            .map(LocalizedTourEvent::from)
            .toList();
    }
}
