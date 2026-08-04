package live.lbtrip.domain.recommendation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import live.lbtrip.domain.propensity.service.PropensityFinder;
import live.lbtrip.domain.region.model.RegionCandidate;
import live.lbtrip.domain.region.repository.RegionCandidateRepository;
import live.lbtrip.domain.recommendation.model.vo.CourseCandidateGroup;
import live.lbtrip.domain.recommendation.model.vo.CourseComposition;
import live.lbtrip.domain.recommendation.model.vo.CourseComposition.CoursePlan;
import live.lbtrip.domain.recommendation.model.vo.RegionPlan;
import live.lbtrip.domain.tourism.client.dto.RegionStats;
import live.lbtrip.domain.tourism.model.entity.TourPlace;
import live.lbtrip.domain.tourism.model.entity.TourRegionStats;
import live.lbtrip.domain.tourism.repository.OdiiThemeRepository;
import live.lbtrip.domain.tourism.repository.TourPlaceRepository;
import live.lbtrip.domain.tourism.repository.TourRegionStatsRepository;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import live.lbtrip.support.fixture.AuthResponseFixture;
import live.lbtrip.support.fixture.PropensityFixture;
import live.lbtrip.support.fixture.RecommendationFixture;

@ExtendWith(MockitoExtension.class)
class RecommendationGenerationServiceTest {

    @Mock
    private RegionCandidateRepository regionCandidateRepository;

    @Mock
    private RegionCandidate regionCandidate;

    @Mock
    private TourRegionStatsRepository tourRegionStatsRepository;

    @Mock
    private TourRegionStats tourRegionStats;

    @Mock
    private TourPlaceRepository tourPlaceRepository;

    @Mock
    private OdiiThemeRepository odiiThemeRepository;

    @Mock
    private RegionScorer regionScorer;

    @Mock
    private CourseComposer courseComposer;

    @Mock
    private CourseCandidateClusterer courseCandidateClusterer;

    @Mock
    private CourseRouteOptimizer courseRouteOptimizer;

    @Mock
    private RecommendationStore recommendationStore;

    @Mock
    private PropensityFinder propensityFinder;

    @InjectMocks
    private RecommendationGenerationService recommendationGenerationService;

    @Test
    void 관광_통계가_없으면_데이터_준비_예외를_던진다() {
        when(propensityFinder.findByUserId(AuthResponseFixture.USER_ID))
            .thenReturn(PropensityFixture.propensity());
        when(regionCandidateRepository.findAll()).thenReturn(List.of());

        assertThatThrownBy(() -> recommendationGenerationService.createRecommendations(AuthResponseFixture.USER_ID))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.TOUR_DATA_NOT_READY);
    }

    @Test
    void 좌표_후보군에서_고른_장소를_최적화된_순서로_저장한다() {
        RegionStats stats = prepareRegion();
        List<TourPlace> candidates = RecommendationFixture.tourPlaces();
        List<CourseCandidateGroup> groups = List.of(CourseCandidateGroup.of("G1", candidates));
        CourseComposition composition = CourseComposition.of("지역 추천 이유", List.of(
            CoursePlan.of("G1", RecommendationFixture.COURSE_NAME, RecommendationFixture.COURSE_REASON,
                List.of("300", "100", "200"))));
        when(regionScorer.selectTop(any(), anyList(), eq(5))).thenReturn(List.of(stats));
        when(tourPlaceRepository.findAllByLdongRegnCdAndLdongSignguCdOrderByContentTypeIdAscSortOrderAsc(
            RecommendationFixture.LDONG_REGN_CD, RecommendationFixture.LDONG_SIGNGU_CD))
            .thenReturn(candidates);
        when(courseCandidateClusterer.cluster(candidates)).thenReturn(groups);
        when(courseComposer.composeGrouped(any(), eq(RecommendationFixture.REGION_NAME), eq(groups)))
            .thenReturn(composition);
        when(odiiThemeRepository.findAllByLongitudeBetweenAndLatitudeBetween(
            anyDouble(), anyDouble(), anyDouble(), anyDouble())).thenReturn(List.of());
        when(courseRouteOptimizer.optimize(anyList()))
            .thenReturn(List.of(candidates.get(0), candidates.get(1), candidates.get(2)));
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<RegionPlan>> plansCaptor = ArgumentCaptor.forClass(List.class);

        recommendationGenerationService.createRecommendations(AuthResponseFixture.USER_ID);

        verify(recommendationStore).replace(eq(AuthResponseFixture.USER_ID), plansCaptor.capture());
        RegionPlan.CoursePlanData course = plansCaptor.getValue().getFirst().courses().getFirst();
        assertThat(course.places()).extracting(RegionPlan.PlaceSnapshot::name)
            .containsExactly("죽녹원", "관방제림", "담양시장");
        assertThat(course.places()).extracting(RegionPlan.PlaceSnapshot::visitOrder)
            .containsExactly(1, 2, 3);
        assertThat(course.places().getFirst().walkMinutes()).isNull();
        assertThat(course.places().get(1).walkMinutes()).isPositive();
    }

    @Test
    void 유효한_좌표_장소가_3개_미만이면_지역을_건너뛴다() {
        RegionStats stats = prepareRegion();
        List<TourPlace> candidates = RecommendationFixture.tourPlaces().subList(0, 2);
        when(regionScorer.selectTop(any(), anyList(), eq(5))).thenReturn(List.of(stats));
        when(tourPlaceRepository.findAllByLdongRegnCdAndLdongSignguCdOrderByContentTypeIdAscSortOrderAsc(
            RecommendationFixture.LDONG_REGN_CD, RecommendationFixture.LDONG_SIGNGU_CD))
            .thenReturn(candidates);
        when(courseCandidateClusterer.cluster(candidates)).thenReturn(List.of());

        assertThatThrownBy(() -> recommendationGenerationService.createRecommendations(AuthResponseFixture.USER_ID))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.RECOMMENDATION_GENERATION_FAILED);
        verifyNoInteractions(courseComposer, recommendationStore);
    }

    private RegionStats prepareRegion() {
        when(propensityFinder.findByUserId(AuthResponseFixture.USER_ID))
            .thenReturn(PropensityFixture.propensity());
        when(regionCandidateRepository.findAll()).thenReturn(List.of(regionCandidate));
        when(regionCandidate.getLdongRegnCd()).thenReturn(RecommendationFixture.LDONG_REGN_CD);
        when(regionCandidate.getLdongSignguCd()).thenReturn(RecommendationFixture.LDONG_SIGNGU_CD);
        when(regionCandidate.getName()).thenReturn(RecommendationFixture.REGION_NAME);
        when(tourRegionStatsRepository.findByLdongRegnCdAndLdongSignguCd(
            RecommendationFixture.LDONG_REGN_CD, RecommendationFixture.LDONG_SIGNGU_CD))
            .thenReturn(Optional.of(tourRegionStats));
        when(tourRegionStats.getLdongRegnCd()).thenReturn(RecommendationFixture.LDONG_REGN_CD);
        when(tourRegionStats.getLdongSignguCd()).thenReturn(RecommendationFixture.LDONG_SIGNGU_CD);
        when(tourRegionStats.toTypeCounts()).thenReturn(Map.of());
        return new RegionStats(
            RecommendationFixture.REGION_NAME,
            RecommendationFixture.LDONG_REGN_CD,
            RecommendationFixture.LDONG_SIGNGU_CD,
            3,
            3,
            Map.of()
        );
    }
}
