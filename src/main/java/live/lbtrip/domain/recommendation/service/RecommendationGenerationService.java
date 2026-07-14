package live.lbtrip.domain.recommendation.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import live.lbtrip.domain.propensity.model.Propensity;
import live.lbtrip.domain.propensity.repository.PropensityRepository;
import live.lbtrip.domain.recommendation.client.OdiiClient;
import live.lbtrip.domain.recommendation.client.TourApiClient;
import live.lbtrip.domain.recommendation.client.dto.OdiiTheme;
import live.lbtrip.domain.recommendation.client.dto.RegionStats;
import live.lbtrip.domain.recommendation.client.dto.TourPlace;
import live.lbtrip.domain.recommendation.model.entity.RegionCandidate;
import live.lbtrip.domain.recommendation.model.enums.TourContentType;
import live.lbtrip.domain.recommendation.model.vo.CourseComposition;
import live.lbtrip.domain.recommendation.model.vo.RegionPlan;
import live.lbtrip.domain.recommendation.model.vo.RegionPlan.CoursePlanData;
import live.lbtrip.domain.recommendation.model.vo.RegionPlan.PlaceSnapshot;
import live.lbtrip.domain.recommendation.repository.RegionCandidateRepository;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 성향 기반 코스 추천 생성 파이프라인.
 * 외부 API(TourAPI, Odii)를 장시간 순차 호출하므로 트랜잭션 없이 실행하고,
 * 결과 저장만 {@link RecommendationStore}가 별도 트랜잭션으로 처리한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationGenerationService {

    private static final int MAX_REGIONS = 5;
    private static final double AUDIO_MATCH_RADIUS_METERS = 500;

    private final PropensityRepository propensityRepository;
    private final RegionCandidateRepository regionCandidateRepository;
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
