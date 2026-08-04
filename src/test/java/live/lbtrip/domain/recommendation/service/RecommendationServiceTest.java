package live.lbtrip.domain.recommendation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import live.lbtrip.domain.incentive.service.IncentiveFinder;
import live.lbtrip.domain.recommendation.dto.response.CourseCandidateResponse;
import live.lbtrip.domain.recommendation.dto.response.CourseDetailResponse;
import live.lbtrip.domain.recommendation.dto.response.RegionRecommendationResponse;
import live.lbtrip.domain.recommendation.model.entity.GeneratedCourse;
import live.lbtrip.domain.recommendation.model.entity.RecommendedRegion;
import live.lbtrip.support.fixture.AuthResponseFixture;
import live.lbtrip.support.fixture.RecommendationFixture;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

    @Mock
    private RecommendedRegionFinder recommendedRegionFinder;

    @Mock
    private GeneratedCourseFinder generatedCourseFinder;

    @Mock
    private IncentiveFinder incentiveFinder;

    @InjectMocks
    private RecommendationService recommendationService;

    @Nested
    class 지역_조회 {

        @Test
        void 사용자의_추천_지역을_응답한다() {
            RecommendedRegion region = RecommendationFixture.region();
            when(recommendedRegionFinder.findAllByUserId(AuthResponseFixture.USER_ID))
                .thenReturn(List.of(region));

            List<RegionRecommendationResponse> responses =
                recommendationService.getRecommendedRegions(AuthResponseFixture.USER_ID);

            assertThat(responses).singleElement().satisfies(response -> {
                assertThat(response.regionId()).isEqualTo(RecommendationFixture.REGION_ID);
                assertThat(response.regionName()).isEqualTo(RecommendationFixture.REGION_NAME);
            });
        }

        @Test
        void 사용자와_지역_ID로_코스를_조회한다() {
            RecommendedRegion region = RecommendationFixture.region();
            when(recommendedRegionFinder.findByIdAndUserId(
                RecommendationFixture.REGION_ID, AuthResponseFixture.USER_ID)).thenReturn(region);

            List<CourseCandidateResponse> responses = recommendationService.getRegionCourses(
                AuthResponseFixture.USER_ID, RecommendationFixture.REGION_ID);

            assertThat(responses).singleElement()
                .extracting(CourseCandidateResponse::courseId)
                .isEqualTo(RecommendationFixture.COURSE_ID);
        }
    }

    @Nested
    class 코스_상세_조회 {

        @Test
        void 사용자와_코스_ID로_상세와_혜택을_조회한다() {
            RecommendedRegion region = RecommendationFixture.region();
            GeneratedCourse course = region.getCourses().getFirst();
            when(generatedCourseFinder.findByIdAndUserId(
                RecommendationFixture.COURSE_ID, AuthResponseFixture.USER_ID)).thenReturn(course);
            when(incentiveFinder.findAllByRegion(
                RecommendationFixture.LDONG_REGN_CD, RecommendationFixture.LDONG_SIGNGU_CD))
                .thenReturn(List.of());

            CourseDetailResponse response = recommendationService.getCourseDetail(
                AuthResponseFixture.USER_ID, RecommendationFixture.COURSE_ID);

            assertThat(response.courseId()).isEqualTo(RecommendationFixture.COURSE_ID);
            assertThat(response.regionName()).isEqualTo(RecommendationFixture.REGION_NAME);
            assertThat(response.places()).hasSize(2);
            assertThat(response.benefits()).isEmpty();
        }
    }
}
