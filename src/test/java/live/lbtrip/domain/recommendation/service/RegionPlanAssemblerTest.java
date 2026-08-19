package live.lbtrip.domain.recommendation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import live.lbtrip.domain.propensity.model.Propensity;
import live.lbtrip.domain.recommendation.model.vo.CourseComposition;
import live.lbtrip.domain.recommendation.model.vo.CourseComposition.CoursePlan;
import live.lbtrip.domain.recommendation.model.vo.RegionPlan;
import live.lbtrip.domain.recommendation.model.vo.RoutedPlace;
import live.lbtrip.domain.recommendation.model.vo.WalkableCluster;
import live.lbtrip.domain.tourism.model.entity.TourPlace;
import live.lbtrip.domain.tourism.model.vo.RegionMetrics;
import live.lbtrip.domain.tourism.service.TourPlaceFinder;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import live.lbtrip.support.fixture.PropensityFixture;
import live.lbtrip.support.fixture.RecommendationFixture;
import live.lbtrip.support.fixture.RegionMetricsFixture;

@ExtendWith(MockitoExtension.class)
class RegionPlanAssemblerTest {

    @Mock
    private TourPlaceFinder tourPlaceFinder;

    @Mock
    private WalkableClusterBuilder walkableClusterBuilder;

    @Mock
    private CourseComposer courseComposer;

    @Mock
    private CourseRoutePlanner courseRoutePlanner;

    @InjectMocks
    private RegionPlanAssembler regionPlanAssembler;

    @Test
    void 검증된_코스의_장소를_역참조해_동선이_확정된_지역_계획을_만든다() {
        Propensity propensity = PropensityFixture.propensity();
        RegionMetrics region = RegionMetricsFixture.로컬실속_지역();
        List<TourPlace> places = RecommendationFixture.tourPlaces();
        CourseComposition composition = CourseComposition.of("추천 이유", List.of(
            CoursePlan.of("코스", "코스 이유", List.of("300", "100", "200"))));
        List<TourPlace> selectedInOrder = List.of(places.get(2), places.get(0), places.get(1));
        List<RoutedPlace> routed = List.of(
            RoutedPlace.of(places.get(0), null),
            RoutedPlace.of(places.get(1), 5),
            RoutedPlace.of(places.get(2), 7));
        List<WalkableCluster> clusters = RecommendationFixture.walkableClusters(places);
        when(tourPlaceFinder.findAllByRegionCandidateId(region.regionCandidateId(), Locale.KOREAN)).thenReturn(places);
        when(walkableClusterBuilder.build(places)).thenReturn(clusters);
        when(courseComposer.compose(propensity, region.regionName(), clusters, Locale.KOREAN)).thenReturn(composition);
        when(courseRoutePlanner.plan(selectedInOrder)).thenReturn(routed);

        List<RegionPlan> result = regionPlanAssembler.assemble(propensity, List.of(region), Locale.KOREAN);

        assertThat(result).singleElement().satisfies(plan -> {
            assertThat(plan.region()).isEqualTo(region);
            assertThat(plan.regionReason()).isEqualTo("추천 이유");
            assertThat(plan.courses()).singleElement().satisfies(course -> {
                assertThat(course.name()).isEqualTo("코스");
                assertThat(course.reason()).isEqualTo("코스 이유");
                assertThat(course.places()).isEqualTo(routed);
            });
        });
    }

    @Test
    void 구성에_실패한_지역은_건너뛰고_나머지를_수집한다() {
        Propensity propensity = PropensityFixture.propensity();
        RegionMetrics failing = RegionMetricsFixture.핫플럭셔리_지역();
        RegionMetrics succeeding = RegionMetricsFixture.로컬실속_지역();
        List<TourPlace> places = RecommendationFixture.tourPlaces();
        CourseComposition composition = CourseComposition.of("추천 이유", List.of(
            CoursePlan.of("코스", "코스 이유", List.of("100", "200", "300"))));
        List<WalkableCluster> clusters = RecommendationFixture.walkableClusters(places);
        when(tourPlaceFinder.findAllByRegionCandidateId(failing.regionCandidateId(), Locale.KOREAN)).thenReturn(places);
        when(tourPlaceFinder.findAllByRegionCandidateId(succeeding.regionCandidateId(), Locale.KOREAN)).thenReturn(places);
        when(walkableClusterBuilder.build(places)).thenReturn(clusters);
        when(courseComposer.compose(propensity, failing.regionName(), clusters, Locale.KOREAN))
            .thenThrow(BusinessException.of(ErrorCode.RECOMMENDATION_GENERATION_FAILED));
        when(courseComposer.compose(propensity, succeeding.regionName(), clusters, Locale.KOREAN)).thenReturn(composition);

        List<RegionPlan> result = regionPlanAssembler.assemble(propensity, List.of(failing, succeeding), Locale.KOREAN);

        assertThat(result).singleElement()
            .satisfies(plan -> assertThat(plan.region()).isEqualTo(succeeding));
    }

    @Test
    void 모든_지역의_구성에_실패하면_추천_생성_예외를_던진다() {
        Propensity propensity = PropensityFixture.propensity();
        RegionMetrics region = RegionMetricsFixture.로컬실속_지역();
        List<TourPlace> places = RecommendationFixture.tourPlaces();
        List<WalkableCluster> clusters = RecommendationFixture.walkableClusters(places);
        when(tourPlaceFinder.findAllByRegionCandidateId(region.regionCandidateId(), Locale.KOREAN)).thenReturn(places);
        when(walkableClusterBuilder.build(places)).thenReturn(clusters);
        when(courseComposer.compose(propensity, region.regionName(), clusters, Locale.KOREAN))
            .thenThrow(BusinessException.of(ErrorCode.RECOMMENDATION_GENERATION_FAILED));

        assertThatThrownBy(() -> regionPlanAssembler.assemble(propensity, List.of(region), Locale.KOREAN))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.RECOMMENDATION_GENERATION_FAILED);
    }

    @Test
    void 도보권_클러스터가_없는_지역은_LLM_호출_없이_건너뛴다() {
        Propensity propensity = PropensityFixture.propensity();
        RegionMetrics scattered = RegionMetricsFixture.핫플럭셔리_지역();
        RegionMetrics succeeding = RegionMetricsFixture.로컬실속_지역();
        List<TourPlace> places = RecommendationFixture.tourPlaces();
        List<TourPlace> scatteredPlaces = RecommendationFixture.manyTourPlaces(2);
        List<WalkableCluster> clusters = RecommendationFixture.walkableClusters(places);
        CourseComposition composition = CourseComposition.of("추천 이유", List.of(
            CoursePlan.of("코스", "코스 이유", List.of("100", "200", "300"))));
        when(tourPlaceFinder.findAllByRegionCandidateId(scattered.regionCandidateId(), Locale.KOREAN)).thenReturn(scatteredPlaces);
        when(tourPlaceFinder.findAllByRegionCandidateId(succeeding.regionCandidateId(), Locale.KOREAN)).thenReturn(places);
        when(walkableClusterBuilder.build(scatteredPlaces)).thenReturn(List.of());
        when(walkableClusterBuilder.build(places)).thenReturn(clusters);
        when(courseComposer.compose(propensity, succeeding.regionName(), clusters, Locale.KOREAN)).thenReturn(composition);

        List<RegionPlan> result = regionPlanAssembler.assemble(propensity, List.of(scattered, succeeding), Locale.KOREAN);

        assertThat(result).singleElement()
            .satisfies(plan -> assertThat(plan.region()).isEqualTo(succeeding));
        verify(courseComposer, never()).compose(eq(propensity), eq(scattered.regionName()), any(), any());
    }

    @Test
    void 구성된_지역이_없으면_추천_생성_예외를_던진다() {
        assertThatThrownBy(() -> regionPlanAssembler.assemble(
            PropensityFixture.propensity(), List.of(), Locale.KOREAN))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.RECOMMENDATION_GENERATION_FAILED);
    }
}
