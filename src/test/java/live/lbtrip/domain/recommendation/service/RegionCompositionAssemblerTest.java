package live.lbtrip.domain.recommendation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import live.lbtrip.domain.propensity.model.Propensity;
import live.lbtrip.domain.recommendation.model.vo.CourseComposition;
import live.lbtrip.domain.recommendation.model.vo.CourseComposition.CoursePlan;
import live.lbtrip.domain.recommendation.model.vo.RegionComposition;
import live.lbtrip.domain.tourism.model.entity.TourPlace;
import live.lbtrip.domain.tourism.model.vo.RegionMetrics;
import live.lbtrip.domain.tourism.service.TourPlaceFinder;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import live.lbtrip.support.fixture.PropensityFixture;
import live.lbtrip.support.fixture.RecommendationFixture;
import live.lbtrip.support.fixture.RegionMetricsFixture;

@ExtendWith(MockitoExtension.class)
class RegionCompositionAssemblerTest {

    @Mock
    private TourPlaceFinder tourPlaceFinder;

    @Mock
    private CourseComposer courseComposer;

    @InjectMocks
    private RegionCompositionAssembler regionCompositionAssembler;

    @Test
    void 지역별로_장소를_조회해_코스_구성_결과를_수집한다() {
        Propensity propensity = PropensityFixture.propensity();
        RegionMetrics region = RegionMetricsFixture.로컬실속_지역();
        List<TourPlace> places = RecommendationFixture.tourPlaces();
        CourseComposition composition = CourseComposition.of("추천 이유", List.of(
            CoursePlan.of("코스", "코스 이유", List.of("100", "200", "300"))));
        when(tourPlaceFinder.findAllByRegionCandidateId(region.regionCandidateId())).thenReturn(places);
        when(courseComposer.compose(propensity, region.regionName(), places)).thenReturn(composition);

        List<RegionComposition> result = regionCompositionAssembler.assemble(propensity, List.of(region));

        assertThat(result).singleElement().satisfies(regionComposition -> {
            assertThat(regionComposition.region()).isEqualTo(region);
            assertThat(regionComposition.composition()).isEqualTo(composition);
        });
    }

    @Test
    void 구성된_지역이_없으면_추천_생성_예외를_던진다() {
        assertThatThrownBy(() -> regionCompositionAssembler.assemble(
            PropensityFixture.propensity(), List.of()))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.RECOMMENDATION_GENERATION_FAILED);
    }
}
