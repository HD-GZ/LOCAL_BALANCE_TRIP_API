package live.lbtrip.domain.recommendation.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import live.lbtrip.domain.propensity.model.Propensity;
import live.lbtrip.domain.propensity.repository.PropensityRepository;
import live.lbtrip.domain.recommendation.client.dto.RegionStats;
import live.lbtrip.domain.recommendation.model.entity.OdiiTheme;
import live.lbtrip.domain.recommendation.model.entity.RegionCandidate;
import live.lbtrip.domain.recommendation.model.entity.TourPlace;
import live.lbtrip.domain.recommendation.model.vo.CourseComposition;
import live.lbtrip.domain.recommendation.model.vo.RegionPlan;
import live.lbtrip.domain.recommendation.model.vo.RegionPlan.CoursePlanData;
import live.lbtrip.domain.recommendation.model.vo.RegionPlan.PlaceSnapshot;
import live.lbtrip.domain.recommendation.repository.OdiiThemeRepository;
import live.lbtrip.domain.recommendation.repository.RegionCandidateRepository;
import live.lbtrip.domain.recommendation.repository.TourPlaceRepository;
import live.lbtrip.domain.recommendation.repository.TourRegionStatsRepository;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationGenerationService {

    private static final int MAX_REGIONS = 5;
    private static final double AUDIO_MATCH_RADIUS_METERS = 500;
    private static final double THEME_LOOKUP_LON_DELTA = 0.23;
    private static final double THEME_LOOKUP_LAT_DELTA = 0.18;

    private final PropensityRepository propensityRepository;
    private final RegionCandidateRepository regionCandidateRepository;
    private final TourRegionStatsRepository tourRegionStatsRepository;
    private final TourPlaceRepository tourPlaceRepository;
    private final OdiiThemeRepository odiiThemeRepository;
    private final RegionScorer regionScorer;
    private final CourseComposer courseComposer;
    private final RecommendationStore recommendationStore;

    public void createRecommendations(Long userId) {
        long totalStartedAt = System.nanoTime();
        log.info("추천 생성 시작: userId={}", userId);

        long stageStartedAt = System.nanoTime();
        Propensity propensity = propensityRepository.findByUserId(userId)
            .orElseThrow(() -> BusinessException.of(ErrorCode.PROPENSITY_NOT_FOUND));
        log.info("사용자 성향 조회 성공: userId={}, elapsedMs={}", userId, elapsedMillis(stageStartedAt));

        stageStartedAt = System.nanoTime();
        List<RegionCandidate> regionCandidates = regionCandidateRepository.findAll();
        List<RegionStats> statsList = new ArrayList<>();
        for (RegionCandidate candidate : regionCandidates) {
            tourRegionStatsRepository
                .findByLdongRegnCdAndLdongSignguCd(candidate.getLdongRegnCd(), candidate.getLdongSignguCd())
                .map(stats -> RegionStats.of(stats, candidate.getName()))
                .ifPresentOrElse(statsList::add,
                    () -> log.warn("지역 통계 미적재 - 지역 제외: {}", candidate.getName()));
        }
        if (statsList.isEmpty()) {
            throw BusinessException.of(ErrorCode.TOUR_DATA_NOT_READY);
        }
        log.info("전체 지역 통계 조회 성공: candidateCount={}, statsCount={}, elapsedMs={}",
            regionCandidates.size(), statsList.size(), elapsedMillis(stageStartedAt));

        stageStartedAt = System.nanoTime();
        List<RegionStats> selected = regionScorer.selectTop(propensity, statsList, MAX_REGIONS);
        log.info("추천 지역 선정 성공: selectedCount={}, elapsedMs={}",
            selected.size(), elapsedMillis(stageStartedAt));

        stageStartedAt = System.nanoTime();
        List<RegionPlan> plans = new ArrayList<>();
        for (RegionStats regionStats : selected) {
            long regionPlanStartedAt = System.nanoTime();
            RegionPlan plan = buildRegionPlan(propensity, regionStats);
            if (plan != null) {
                plans.add(plan);
                log.info("지역 추천 계획 생성 성공: region={}, courseCount={}, elapsedMs={}",
                    regionStats.regionName(), plan.courses().size(), elapsedMillis(regionPlanStartedAt));
            }
        }
        if (plans.isEmpty()) {
            throw BusinessException.of(ErrorCode.RECOMMENDATION_GENERATION_FAILED);
        }
        log.info("전체 지역 추천 계획 생성 성공: planCount={}, elapsedMs={}",
            plans.size(), elapsedMillis(stageStartedAt));

        stageStartedAt = System.nanoTime();
        recommendationStore.replace(userId, plans);
        log.info("추천 결과 저장 성공: userId={}, planCount={}, elapsedMs={}",
            userId, plans.size(), elapsedMillis(stageStartedAt));
        log.info("추천 생성 완료: userId={}, elapsedMs={}", userId, elapsedMillis(totalStartedAt));
    }

    private RegionPlan buildRegionPlan(Propensity propensity, RegionStats regionStats) {
        long stageStartedAt = System.nanoTime();
        List<TourPlace> fetched = tourPlaceRepository
            .findAllByLdongRegnCdAndLdongSignguCdOrderByContentTypeIdAscSortOrderAsc(
                regionStats.ldongRegnCd(), regionStats.ldongSignguCd());
        Map<String, TourPlace> candidatesById = new LinkedHashMap<>();
        for (TourPlace place : fetched) {
            candidatesById.putIfAbsent(place.getContentId(), place);
        }
        if (candidatesById.isEmpty()) {
            log.warn("후보 장소 없음 - 지역 제외: {}", regionStats.regionName());
            return null;
        }
        log.info("지역 후보 장소 조회 성공: region={}, placeCount={}, elapsedMs={}",
            regionStats.regionName(), candidatesById.size(), elapsedMillis(stageStartedAt));

        List<TourPlace> candidates = List.copyOf(candidatesById.values());
        stageStartedAt = System.nanoTime();
        CourseComposition composition = courseComposer.compose(
            propensity, regionStats.regionName(), candidates);
        log.info("LLM 코스 구성 성공: region={}, courseCount={}, elapsedMs={}",
            regionStats.regionName(), composition.courses().size(), elapsedMillis(stageStartedAt));

        stageStartedAt = System.nanoTime();
        double centerLongitude = averageLongitude(candidates);
        double centerLatitude = averageLatitude(candidates);
        List<OdiiTheme> themes = odiiThemeRepository.findAllByLongitudeBetweenAndLatitudeBetween(
            centerLongitude - THEME_LOOKUP_LON_DELTA, centerLongitude + THEME_LOOKUP_LON_DELTA,
            centerLatitude - THEME_LOOKUP_LAT_DELTA, centerLatitude + THEME_LOOKUP_LAT_DELTA);
        log.info("Odii 테마 조회 완료: region={}, themeCount={}, elapsedMs={}",
            regionStats.regionName(), themes.size(), elapsedMillis(stageStartedAt));

        stageStartedAt = System.nanoTime();
        List<CoursePlanData> courses = new ArrayList<>();
        for (CourseComposition.CoursePlan coursePlan : composition.courses()) {
            courses.add(buildCourse(coursePlan, candidatesById, themes));
        }
        log.info("코스 상세 구성 성공: region={}, courseCount={}, placeCount={}, elapsedMs={}",
            regionStats.regionName(), courses.size(),
            courses.stream().mapToInt(course -> course.places().size()).sum(), elapsedMillis(stageStartedAt));

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
        for (String contentId : coursePlan.placeContentIds()) {
            TourPlace place = candidatesById.get(contentId);
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

    private long elapsedMillis(long startedAt) {
        return (System.nanoTime() - startedAt) / 1_000_000;
    }
}
