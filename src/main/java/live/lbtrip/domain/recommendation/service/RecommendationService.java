package live.lbtrip.domain.recommendation.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import live.lbtrip.domain.propensity.model.Propensity;
import live.lbtrip.domain.propensity.repository.PropensityRepository;
import live.lbtrip.domain.recommendation.client.OdiiClient;
import live.lbtrip.domain.recommendation.client.TourApiClient;
import live.lbtrip.domain.recommendation.client.dto.OdiiTheme;
import live.lbtrip.domain.recommendation.client.dto.RegionStats;
import live.lbtrip.domain.recommendation.client.dto.TourPlace;
import live.lbtrip.domain.recommendation.dto.response.CourseCandidateResponse;
import live.lbtrip.domain.recommendation.dto.response.CourseDetailResponse;
import live.lbtrip.domain.recommendation.dto.response.RegionRecommendationResponse;
import live.lbtrip.domain.recommendation.model.CourseComposition;
import live.lbtrip.domain.recommendation.model.RegionPlan;
import live.lbtrip.domain.recommendation.model.RegionPlan.CoursePlanData;
import live.lbtrip.domain.recommendation.model.RegionPlan.PlaceSnapshot;
import live.lbtrip.domain.recommendation.model.TourContentType;
import live.lbtrip.domain.recommendation.model.entity.GeneratedCourse;
import live.lbtrip.domain.recommendation.model.entity.RecommendedRegion;
import live.lbtrip.domain.recommendation.model.entity.RegionCandidate;
import live.lbtrip.domain.recommendation.repository.GeneratedCourseRepository;
import live.lbtrip.domain.recommendation.repository.RecommendedRegionRepository;
import live.lbtrip.domain.recommendation.repository.RegionCandidateRepository;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationService {

    private static final int MAX_REGIONS = 5;
    private static final double AUDIO_MATCH_RADIUS_METERS = 500;

    private final PropensityRepository propensityRepository;
    private final RegionCandidateRepository regionCandidateRepository;
    private final RecommendedRegionRepository recommendedRegionRepository;
    private final GeneratedCourseRepository generatedCourseRepository;
    private final TourApiClient tourApiClient;
    private final OdiiClient odiiClient;
    private final RegionScorer regionScorer;
    private final CourseComposer courseComposer;
    private final RecommendationStore recommendationStore;

    public void createRecommendations(Long userId) {
        Propensity propensity = propensityRepository.findByUserId(userId)
            .orElseThrow(() -> BusinessException.of(ErrorCode.PROPENSITY_NOT_FOUND));

        List<RegionStats> statsList = new ArrayList<>();
        for (RegionCandidate candidate : regionCandidateRepository.findAll()) {
            statsList.add(tourApiClient.fetchRegionStats(candidate));
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

    @Transactional(readOnly = true)
    public List<RegionRecommendationResponse> getRecommendedRegions(Long userId) {
        return recommendedRegionRepository.findAllByUserIdOrderByDisplayOrder(userId).stream()
            .map(RegionRecommendationResponse::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<CourseCandidateResponse> getRegionCourses(Long userId, Long regionId) {
        RecommendedRegion region = recommendedRegionRepository.findByIdAndUserId(regionId, userId)
            .orElseThrow(() -> BusinessException.of(ErrorCode.REGION_NOT_FOUND));

        return region.getCourses().stream()
            .map(CourseCandidateResponse::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public CourseDetailResponse getCourseDetail(Long userId, Long courseId) {
        GeneratedCourse course = generatedCourseRepository.findByIdAndUserId(courseId, userId)
            .orElseThrow(() -> BusinessException.of(ErrorCode.COURSE_NOT_FOUND));

        return CourseDetailResponse.of(course);
    }

    private RegionPlan buildRegionPlan(Propensity propensity, RegionStats regionStats) {
        Map<String, TourPlace> candidatesById = new LinkedHashMap<>();
        for (TourContentType contentType : TourContentType.courseCandidates()) {
            for (TourPlace place : tourApiClient.fetchPlaces(
                    regionStats.ldongRegnCd(), regionStats.ldongSignguCd(), contentType.getCode())) {
                candidatesById.putIfAbsent(place.contentId(), place);
            }
        }
        if (candidatesById.isEmpty()) {
            log.warn("후보 장소 없음 - 지역 제외: {}", regionStats.regionName());
            return null;
        }

        List<TourPlace> candidates = List.copyOf(candidatesById.values());
        CourseComposition composition = courseComposer.compose(
            propensity, regionStats.regionName(), candidates);

        List<OdiiTheme> themes = odiiClient.fetchThemesNear(
            averageLongitude(candidates), averageLatitude(candidates));

        List<CoursePlanData> courses = new ArrayList<>();
        for (CourseComposition.CoursePlan coursePlan : composition.courses()) {
            courses.add(buildCourse(coursePlan, candidatesById, themes));
        }

        return RegionPlan.of(
            regionStats.regionName(), courses.getFirst().imageUrl(), composition.regionReason(), courses);
    }

    private CoursePlanData buildCourse(
        CourseComposition.CoursePlan coursePlan,
        Map<String, TourPlace> candidatesById,
        List<OdiiTheme> themes
    ) {
        List<PlaceSnapshot> places = new ArrayList<>();
        TourPlace previous = null;
        int visitOrder = 1;
        for (String contentId : coursePlan.placeContentIds()) {
            TourPlace place = candidatesById.get(contentId);
            String overview = tourApiClient.fetchOverview(contentId);
            Integer walkMinutes = previous == null ? null : WalkTimeCalculator.walkMinutes(
                previous.longitude(), previous.latitude(), place.longitude(), place.latitude());
            String audioUrl = matchAudio(place, themes);

            places.add(PlaceSnapshot.of(
                visitOrder++, place.title(), overview, place.imageUrl(),
                place.latitude(), place.longitude(), walkMinutes,
                audioUrl != null, audioUrl));
            previous = place;
        }
        return CoursePlanData.of(
            coursePlan.name(), coursePlan.reason(), places.getFirst().imageUrl(), places);
    }

    private String matchAudio(TourPlace place, List<OdiiTheme> themes) {
        String placeTitle = normalizeTitle(place.title());
        for (OdiiTheme theme : themes) {
            Double distance = WalkTimeCalculator.distanceMeters(
                place.longitude(), place.latitude(), theme.longitude(), theme.latitude());
            if (distance == null || distance > AUDIO_MATCH_RADIUS_METERS) {
                continue;
            }
            String themeTitle = normalizeTitle(theme.title());
            if (placeTitle.contains(themeTitle) || themeTitle.contains(placeTitle)) {
                return odiiClient.fetchFirstAudioUrl(theme.tid(), theme.tlid());
            }
        }
        return null;
    }

    private String normalizeTitle(String title) {
        return title == null ? "" : title.replaceAll("\\s", "");
    }

    private double averageLongitude(List<TourPlace> places) {
        return places.stream().filter(place -> place.longitude() != null)
            .mapToDouble(TourPlace::longitude).average().orElse(0);
    }

    private double averageLatitude(List<TourPlace> places) {
        return places.stream().filter(place -> place.latitude() != null)
            .mapToDouble(TourPlace::latitude).average().orElse(0);
    }
}
