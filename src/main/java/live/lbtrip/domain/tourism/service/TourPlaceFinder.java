package live.lbtrip.domain.tourism.service;

import java.util.List;

import org.springframework.stereotype.Component;

import live.lbtrip.domain.tourism.model.entity.TourPlace;
import live.lbtrip.domain.tourism.repository.TourPlaceRepository;
import live.lbtrip.global.i18n.LocaleConfig;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TourPlaceFinder {

    private final TourPlaceRepository tourPlaceRepository;

    public List<TourPlace> findAllByRegionCandidateId(Long regionCandidateId) {
        return tourPlaceRepository.findAllWithOdiiThemeByLocaleAndRegionCandidateId(
            LocaleConfig.DEFAULT_LOCALE, regionCandidateId);
    }
}
