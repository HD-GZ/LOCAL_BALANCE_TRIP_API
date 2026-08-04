package live.lbtrip.domain.recommendation.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import live.lbtrip.domain.propensity.model.Propensity;
import live.lbtrip.domain.propensity.service.PropensityFinder;
import live.lbtrip.domain.region.model.RegionCandidate;
import live.lbtrip.domain.region.repository.RegionCandidateRepository;
import live.lbtrip.domain.recommendation.model.vo.CourseCandidateGroup;
import live.lbtrip.domain.recommendation.model.vo.CourseComposition;
import live.lbtrip.domain.recommendation.model.vo.RegionPlan;
import live.lbtrip.domain.recommendation.model.vo.RegionPlan.CoursePlanData;
import live.lbtrip.domain.recommendation.model.vo.RegionPlan.PlaceSnapshot;
import live.lbtrip.domain.tourism.client.dto.RegionStats;
import live.lbtrip.domain.tourism.model.entity.OdiiTheme;
import live.lbtrip.domain.tourism.model.entity.TourPlace;
import live.lbtrip.domain.tourism.repository.OdiiThemeRepository;
import live.lbtrip.domain.tourism.repository.TourPlaceRepository;
import live.lbtrip.domain.tourism.repository.TourRegionStatsRepository;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RecommendationGenerationService {

    private static final int MAX_REGIONS = 5;
    private static final double AUDIO_MATCH_RADIUS_METERS = 500;
    private static final double THEME_LOOKUP_LON_DELTA = 0.23;
    private static final double THEME_LOOKUP_LAT_DELTA = 0.18;

    private final RegionCandidateRepository regionCandidateRepository;
    private final TourRegionStatsRepository tourRegionStatsRepository;
    private final TourPlaceRepository tourPlaceRepository;
    private final OdiiThemeRepository odiiThemeRepository;
    private final RegionScorer regionScorer;
    private final CourseComposer courseComposer;
    private final CourseCandidateClusterer courseCandidateClusterer;
    private final CourseRouteOptimizer courseRouteOptimizer;
    private final RecommendationStore recommendationStore;
    private final PropensityFinder propensityFinder;

    public void createRecommendations(Long userId) {
        Propensity propensity = propensityFinder.findByUserId(userId);
        List<RegionCandidate> regionCandidates = regionCandidateRepository.findAll();
        List<RegionStats> statsList = new ArrayList<>();
        for (RegionCandidate candidate : regionCandidates) {
            tourRegionStatsRepository
                .findByLdongRegnCdAndLdongSignguCd(candidate.getLdongRegnCd(), candidate.getLdongSignguCd())
                .map(stats -> RegionStats.of(stats, candidate.getName()))
                .ifPresent(statsList::add);
        }
        if (statsList.isEmpty()) {
            throw BusinessException.of(ErrorCode.TOUR_DATA_NOT_READY);
        }

        List<RegionStats> selected = regionScorer.selectTop(propensity, statsList, MAX_REGIONS);

        List<RegionPlan> plans = new ArrayList<>();
        for (RegionStats regionStats : selected) {
            RegionPlan plan = buildRegionPlan(propensity, regionStats);
            if (plan != null) {
                plans.add(plan);
            }
        }
        if (plans.isEmpty()) {
            throw BusinessException.of(ErrorCode.RECOMMENDATION_GENERATION_FAILED);
        }

        recommendationStore.replace(userId, plans);
    }

    private RegionPlan buildRegionPlan(Propensity propensity, RegionStats regionStats) {
        List<TourPlace> fetched = tourPlaceRepository
            .findAllByLdongRegnCdAndLdongSignguCdOrderByContentTypeIdAscSortOrderAsc(
                regionStats.ldongRegnCd(), regionStats.ldongSignguCd());
        Map<String, TourPlace> candidatesById = new LinkedHashMap<>();
        for (TourPlace place : fetched) {
            candidatesById.putIfAbsent(place.getContentId(), place);
        }
        if (candidatesById.isEmpty()) {
            return null;
        }

        List<TourPlace> candidates = List.copyOf(candidatesById.values());
        List<CourseCandidateGroup> candidateGroups = courseCandidateClusterer.cluster(candidates);
        if (candidateGroups.isEmpty()) {
            return null;
        }
        CourseComposition composition = courseComposer.composeGrouped(
            propensity, regionStats.regionName(), candidateGroups);

        List<TourPlace> groupedCandidates = candidateGroups.stream()
            .flatMap(group -> group.candidates().stream())
            .distinct()
            .toList();
        double centerLongitude = averageLongitude(groupedCandidates);
        double centerLatitude = averageLatitude(groupedCandidates);
        List<OdiiTheme> themes = odiiThemeRepository.findAllByLongitudeBetweenAndLatitudeBetween(
            centerLongitude - THEME_LOOKUP_LON_DELTA, centerLongitude + THEME_LOOKUP_LON_DELTA,
            centerLatitude - THEME_LOOKUP_LAT_DELTA, centerLatitude + THEME_LOOKUP_LAT_DELTA);

        List<CoursePlanData> courses = new ArrayList<>();
        for (CourseComposition.CoursePlan coursePlan : composition.courses()) {
            courses.add(buildCourse(coursePlan, candidatesById, themes));
        }

        return RegionPlan.of(
            regionStats.regionName(), regionStats.ldongRegnCd(), regionStats.ldongSignguCd(),
            courses.getFirst().imageUrl(), composition.regionReason(), courses);
    }

    private CoursePlanData buildCourse(
        CourseComposition.CoursePlan coursePlan,
        Map<String, TourPlace> candidatesById,
        List<OdiiTheme> themes
    ) {
        List<PlaceSnapshot> places = new ArrayList<>();
        TourPlace previous = null;
        int visitOrder = 1;
        List<TourPlace> selectedPlaces = coursePlan.placeContentIds().stream()
            .map(candidatesById::get)
            .toList();
        for (TourPlace place : courseRouteOptimizer.optimize(selectedPlaces)) {
            Integer walkMinutes = previous == null ? null : WalkTimeCalculator.walkMinutes(
                previous.getLongitude(), previous.getLatitude(), place.getLongitude(), place.getLatitude());
            String audioUrl = matchAudio(place, themes);

            places.add(PlaceSnapshot.of(
                visitOrder++, place.getTitle(), normalizeOverview(place.getOverview()), place.getImageUrl(),
                place.getLatitude(), place.getLongitude(), walkMinutes,
                audioUrl != null, audioUrl));
            previous = place;
        }
        return CoursePlanData.of(
            coursePlan.name(), coursePlan.reason(), places.getFirst().imageUrl(), places);
    }

    private String matchAudio(TourPlace place, List<OdiiTheme> themes) {
        String placeTitle = normalizeTitle(place.getTitle());
        for (OdiiTheme theme : themes) {
            if (theme.getAudioUrl() == null) {
                continue;
            }
            Double distance = WalkTimeCalculator.distanceMeters(
                place.getLongitude(), place.getLatitude(), theme.getLongitude(), theme.getLatitude());
            if (distance == null || distance > AUDIO_MATCH_RADIUS_METERS) {
                continue;
            }
            String themeTitle = normalizeTitle(theme.getTitle());
            if (placeTitle.contains(themeTitle) || themeTitle.contains(placeTitle)) {
                return theme.getAudioUrl();
            }
        }
        return null;
    }

    private String normalizeTitle(String title) {
        return title == null ? "" : title.replaceAll("\\s", "");
    }

    private String normalizeOverview(String overview) {
        return overview == null || overview.isBlank() ? null : overview;
    }

    private double averageLongitude(List<TourPlace> places) {
        return places.stream().filter(place -> place.getLongitude() != null)
            .mapToDouble(TourPlace::getLongitude).average().orElse(0);
    }

    private double averageLatitude(List<TourPlace> places) {
        return places.stream().filter(place -> place.getLatitude() != null)
            .mapToDouble(TourPlace::getLatitude).average().orElse(0);
    }
}
