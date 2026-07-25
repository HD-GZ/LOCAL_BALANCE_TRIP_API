package live.lbtrip.domain.savedcourse.controller;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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
import live.lbtrip.domain.savedcourse.service.SavedCourseService;
import live.lbtrip.global.config.CorsProperties;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import live.lbtrip.support.fixture.AuthResponseFixture;
import live.lbtrip.support.fixture.TokenFixture;

@WebMvcTest(CourseSaveController.class)
@Import(CourseSaveControllerTest.TestCorsConfig.class)
class CourseSaveControllerTest {

    private static final Long COURSE_ID = 2L;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SavedCourseService savedCourseService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMappingContext;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private AdminJwtTokenProvider adminJwtTokenProvider;

    @Nested
    class 저장 {

        @Test
        void 추천_코스를_저장한다() throws Exception {
            인증된_사용자();

            mockMvc.perform(post("/recommendations/courses/{courseId}/save", COURSE_ID)
                    .header("Authorization", "Bearer " + TokenFixture.ACCESS_TOKEN))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.result").value("SUCCESS"));

            verify(savedCourseService).saveCourse(AuthResponseFixture.USER_ID, COURSE_ID);
        }

        @Test
        void 이미_저장한_코스면_예외를_응답한다() throws Exception {
            인증된_사용자();
            doThrow(BusinessException.of(ErrorCode.DUPLICATE_SAVE_COURSE))
                .when(savedCourseService).saveCourse(AuthResponseFixture.USER_ID, COURSE_ID);

            mockMvc.perform(post("/recommendations/courses/{courseId}/save", COURSE_ID)
                    .header("Authorization", "Bearer " + TokenFixture.ACCESS_TOKEN))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("DUPLICATE_SAVE_COURSE"));
        }

        @Test
        void 인증_토큰이_없으면_예외를_응답한다() throws Exception {
            mockMvc.perform(post("/recommendations/courses/{courseId}/save", COURSE_ID))
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
