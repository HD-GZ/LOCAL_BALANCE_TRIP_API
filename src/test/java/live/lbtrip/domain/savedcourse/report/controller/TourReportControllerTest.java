package live.lbtrip.domain.savedcourse.report.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
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
import live.lbtrip.domain.savedcourse.report.dto.response.TourReportResponse;
import live.lbtrip.domain.savedcourse.report.service.TourReportService;
import live.lbtrip.global.config.CorsProperties;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import live.lbtrip.support.fixture.AuthResponseFixture;
import live.lbtrip.support.fixture.TokenFixture;

@WebMvcTest(TourReportController.class)
@Import(TourReportControllerTest.TestCorsConfig.class)
class TourReportControllerTest {

    private static final Long SAVED_COURSE_ID = 1L;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TourReportService tourReportService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMappingContext;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private AdminJwtTokenProvider adminJwtTokenProvider;

    @Nested
    class 리포트_조회 {

        @Test
        void 투어_리포트를_응답한다() throws Exception {
            인증된_사용자();
            TourReportResponse response = new TourReportResponse(
                "공주 원도심 슬로우 투어",
                "https://images.example.com/course.jpg",
                5,
                130L,
                52000,
                LocalDateTime.of(2026, 7, 17, 15, 30)
            );
            when(tourReportService.getReport(AuthResponseFixture.USER_ID, SAVED_COURSE_ID))
                .thenReturn(response);

            mockMvc.perform(get("/saved-courses/{savedCourseId}/report", SAVED_COURSE_ID)
                    .header("Authorization", "Bearer " + TokenFixture.ACCESS_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.courseName").value("공주 원도심 슬로우 투어"))
                .andExpect(jsonPath("$.data.imageUrl").value("https://images.example.com/course.jpg"))
                .andExpect(jsonPath("$.data.visitedPlaceCount").value(5))
                .andExpect(jsonPath("$.data.durationMinutes").value(130))
                .andExpect(jsonPath("$.data.totalSpentAmount").value(52000));
        }

        @Test
        void 투어를_종료하지_않았으면_예외를_응답한다() throws Exception {
            인증된_사용자();
            when(tourReportService.getReport(AuthResponseFixture.USER_ID, SAVED_COURSE_ID))
                .thenThrow(BusinessException.of(ErrorCode.TOUR_REPORT_NOT_AVAILABLE));

            mockMvc.perform(get("/saved-courses/{savedCourseId}/report", SAVED_COURSE_ID)
                    .header("Authorization", "Bearer " + TokenFixture.ACCESS_TOKEN))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("TOUR_REPORT_NOT_AVAILABLE"));
        }

        @Test
        void 저장_코스가_없으면_예외를_응답한다() throws Exception {
            인증된_사용자();
            when(tourReportService.getReport(AuthResponseFixture.USER_ID, SAVED_COURSE_ID))
                .thenThrow(BusinessException.of(ErrorCode.SAVED_COURSE_NOT_FOUND));

            mockMvc.perform(get("/saved-courses/{savedCourseId}/report", SAVED_COURSE_ID)
                    .header("Authorization", "Bearer " + TokenFixture.ACCESS_TOKEN))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("SAVED_COURSE_NOT_FOUND"));
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
