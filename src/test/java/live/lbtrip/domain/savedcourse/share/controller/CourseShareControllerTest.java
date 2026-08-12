package live.lbtrip.domain.savedcourse.share.controller;

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
import live.lbtrip.domain.savedcourse.model.enums.SavedCourseStatus;
import live.lbtrip.domain.savedcourse.share.dto.response.ShareTokenResponse;
import live.lbtrip.domain.savedcourse.share.dto.response.SharedCourseDetailResponse;
import live.lbtrip.domain.savedcourse.share.service.CourseShareService;
import live.lbtrip.global.config.CorsProperties;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import live.lbtrip.support.fixture.AuthResponseFixture;
import live.lbtrip.support.fixture.CourseShareFixture;
import live.lbtrip.support.fixture.RecommendationFixture;
import live.lbtrip.support.fixture.TokenFixture;
import live.lbtrip.support.fixture.UserFixture;

@WebMvcTest(CourseShareController.class)
@Import(CourseShareControllerTest.TestCorsConfig.class)
class CourseShareControllerTest {

    private static final Long SAVED_COURSE_ID = 3L;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CourseShareService courseShareService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMappingContext;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private AdminJwtTokenProvider adminJwtTokenProvider;

    @Nested
    class 공유_토큰_발급 {

        @Test
        void 공유_토큰을_발급한다() throws Exception {
            인증된_사용자();
            when(courseShareService.issueShareToken(AuthResponseFixture.USER_ID, SAVED_COURSE_ID))
                .thenReturn(new ShareTokenResponse(CourseShareFixture.TOKEN, CourseShareFixture.EXPIRES_AT));

            mockMvc.perform(post("/saved-courses/{savedCourseId}/share-tokens", SAVED_COURSE_ID)
                    .header("Authorization", "Bearer " + TokenFixture.ACCESS_TOKEN))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.token").value(CourseShareFixture.TOKEN))
                .andExpect(jsonPath("$.data.expiresAt").exists());
        }

        @Test
        void 저장한_코스가_없으면_예외를_응답한다() throws Exception {
            인증된_사용자();
            doThrow(BusinessException.of(ErrorCode.SAVED_COURSE_NOT_FOUND))
                .when(courseShareService).issueShareToken(AuthResponseFixture.USER_ID, SAVED_COURSE_ID);

            mockMvc.perform(post("/saved-courses/{savedCourseId}/share-tokens", SAVED_COURSE_ID)
                    .header("Authorization", "Bearer " + TokenFixture.ACCESS_TOKEN))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("SAVED_COURSE_NOT_FOUND"));
        }

        @Test
        void 인증_토큰이_없으면_예외를_응답한다() throws Exception {
            mockMvc.perform(post("/saved-courses/{savedCourseId}/share-tokens", SAVED_COURSE_ID))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("INVALID_ACCESS_TOKEN"));
        }
    }

    @Nested
    class 공유_코스_조회 {

        @Test
        void 인증_없이_공유_코스_상세를_조회한다() throws Exception {
            when(courseShareService.getSharedCourseDetail(CourseShareFixture.TOKEN))
                .thenReturn(new SharedCourseDetailResponse(
                    SAVED_COURSE_ID,
                    UserFixture.NAME,
                    RecommendationFixture.IMAGE_URL,
                    RecommendationFixture.REGION_NAME,
                    RecommendationFixture.COURSE_NAME,
                    SavedCourseStatus.BEFORE_TRIP,
                    List.of(),
                    List.of()));

            mockMvc.perform(get("/shared-courses/{token}", CourseShareFixture.TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.savedCourseId").value(SAVED_COURSE_ID))
                .andExpect(jsonPath("$.data.sharedByName").value(UserFixture.NAME))
                .andExpect(jsonPath("$.data.imageUrl").value(RecommendationFixture.IMAGE_URL))
                .andExpect(jsonPath("$.data.title").value(RecommendationFixture.COURSE_NAME));
        }

        @Test
        void 토큰이_없으면_예외를_응답한다() throws Exception {
            doThrow(BusinessException.of(ErrorCode.SHARE_TOKEN_NOT_FOUND))
                .when(courseShareService).getSharedCourseDetail(CourseShareFixture.TOKEN);

            mockMvc.perform(get("/shared-courses/{token}", CourseShareFixture.TOKEN))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("SHARE_TOKEN_NOT_FOUND"));
        }

        @Test
        void 만료된_토큰이면_예외를_응답한다() throws Exception {
            doThrow(BusinessException.of(ErrorCode.SHARE_TOKEN_EXPIRED))
                .when(courseShareService).getSharedCourseDetail(CourseShareFixture.TOKEN);

            mockMvc.perform(get("/shared-courses/{token}", CourseShareFixture.TOKEN))
                .andExpect(status().isGone())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("SHARE_TOKEN_EXPIRED"));
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
