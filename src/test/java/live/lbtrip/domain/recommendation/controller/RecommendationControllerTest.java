package live.lbtrip.domain.recommendation.controller;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import live.lbtrip.admin.auth.service.AdminJwtTokenProvider;
import live.lbtrip.domain.auth.model.JwtTokenSubject;
import live.lbtrip.domain.auth.service.JwtTokenProvider;
import live.lbtrip.domain.recommendation.service.RecommendationGenerationService;
import live.lbtrip.domain.recommendation.service.RecommendationService;
import live.lbtrip.global.config.CorsProperties;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import live.lbtrip.support.fixture.AuthResponseFixture;
import live.lbtrip.support.fixture.RecommendationFixture;
import live.lbtrip.support.fixture.TokenFixture;

@WebMvcTest(RecommendationController.class)
@Import(RecommendationControllerTest.TestCorsConfig.class)
class RecommendationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RecommendationGenerationService recommendationGenerationService;

    @MockitoBean
    private RecommendationService recommendationService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private AdminJwtTokenProvider adminJwtTokenProvider;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMappingContext;

    @Nested
    class 생성 {

        @Test
        void 코스_추천을_생성한다() throws Exception {
            인증된_사용자();

            mockMvc.perform(post("/recommendations")
                    .header("Authorization", "Bearer " + TokenFixture.ACCESS_TOKEN))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data").doesNotExist());
        }

        @Test
        void 취향_결과가_없으면_예외를_응답한다() throws Exception {
            인증된_사용자();
            doThrow(BusinessException.of(ErrorCode.PROPENSITY_NOT_FOUND))
                .when(recommendationGenerationService).createRecommendations(AuthResponseFixture.USER_ID);

            mockMvc.perform(post("/recommendations")
                    .header("Authorization", "Bearer " + TokenFixture.ACCESS_TOKEN))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("PROPENSITY_NOT_FOUND"));
        }
    }

    @Nested
    class 조회 {

        @Test
        void 추천_지역을_조회한다() throws Exception {
            인증된_사용자();
            when(recommendationService.getRecommendedRegions(AuthResponseFixture.USER_ID))
                .thenReturn(List.of(RecommendationFixture.regionResponse()));

            mockMvc.perform(get("/recommendations/regions")
                    .header("Authorization", "Bearer " + TokenFixture.ACCESS_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data[0].regionId").value(RecommendationFixture.REGION_ID))
                .andExpect(jsonPath("$.data[0].regionName").value(RecommendationFixture.REGION_NAME));
        }

        @Test
        void 추천_지역의_코스를_조회한다() throws Exception {
            인증된_사용자();
            when(recommendationService.getRegionCourses(
                AuthResponseFixture.USER_ID, RecommendationFixture.REGION_ID))
                .thenReturn(List.of(RecommendationFixture.courseResponse()));

            mockMvc.perform(get("/recommendations/regions/{regionId}/courses", RecommendationFixture.REGION_ID)
                    .header("Authorization", "Bearer " + TokenFixture.ACCESS_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data[0].courseId").value(RecommendationFixture.COURSE_ID))
                .andExpect(jsonPath("$.data[0].title").value(RecommendationFixture.COURSE_NAME));
        }

        @Test
        void 코스_상세를_조회한다() throws Exception {
            인증된_사용자();
            when(recommendationService.getCourseDetail(
                AuthResponseFixture.USER_ID, RecommendationFixture.COURSE_ID))
                .thenReturn(RecommendationFixture.courseDetailResponse());

            mockMvc.perform(get("/recommendations/courses/{courseId}", RecommendationFixture.COURSE_ID)
                    .header("Authorization", "Bearer " + TokenFixture.ACCESS_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.courseId").value(RecommendationFixture.COURSE_ID))
                .andExpect(jsonPath("$.data.places[0].order").value(1));
        }

        @Test
        void 인증_토큰이_없으면_예외를_응답한다() throws Exception {
            mockMvc.perform(get("/recommendations/regions"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("INVALID_ACCESS_TOKEN"));
        }
    }

    private void 인증된_사용자() {
        when(jwtTokenProvider.isValid(TokenFixture.ACCESS_TOKEN)).thenReturn(true);
        when(jwtTokenProvider.parseSubject(TokenFixture.ACCESS_TOKEN))
            .thenReturn(JwtTokenSubject.of(AuthResponseFixture.USER_ID));
    }

    @TestConfiguration
    static class TestCorsConfig {

        @Bean
        CorsProperties corsProperties() {
            return new CorsProperties(List.of("http://localhost"));
        }
    }
}
