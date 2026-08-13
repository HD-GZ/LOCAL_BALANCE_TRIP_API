package live.lbtrip.domain.tourism.service;

import java.util.List;

import org.springframework.stereotype.Component;

import live.lbtrip.domain.region.model.RegionCandidate;
import live.lbtrip.domain.tourism.model.entity.OdiiTheme;
import live.lbtrip.domain.tourism.model.entity.TourPlace;
import live.lbtrip.domain.tourism.model.vo.Centroid;
import live.lbtrip.domain.tourism.repository.OdiiThemeRepository;
import live.lbtrip.domain.tourism.repository.TourPlaceRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PlaceThemeLinker {

    private static final double LOOKUP_LON_DELTA = 0.23;
    private static final double LOOKUP_LAT_DELTA = 0.18;

    private final TourPlaceRepository tourPlaceRepository;
    private final OdiiThemeRepository odiiThemeRepository;
    private final OdiiThemeMatcher odiiThemeMatcher;

    public void link(RegionCandidate candidate) {
        List<TourPlace> places = tourPlaceRepository
            .findAllByRegionCandidateIdOrderByContentTypeIdAscSortOrderAsc(candidate.getId());
        Centroid.of(places, TourPlace::getLongitude, TourPlace::getLatitude)
            .ifPresent(centroid -> assignThemes(places, findThemesNear(centroid)));
    }

    private List<OdiiTheme> findThemesNear(Centroid centroid) {
        return odiiThemeRepository.findAllByLongitudeBetweenAndLatitudeBetween(
            centroid.longitude() - LOOKUP_LON_DELTA,
            centroid.longitude() + LOOKUP_LON_DELTA,
            centroid.latitude() - LOOKUP_LAT_DELTA,
            centroid.latitude() + LOOKUP_LAT_DELTA);
    }

    private void assignThemes(List<TourPlace> places, List<OdiiTheme> themes) {
        for (TourPlace place : places) {
            place.assignOdiiTheme(odiiThemeMatcher.match(place, themes).orElse(null));
            tourPlaceRepository.save(place);
        }
    }
}
