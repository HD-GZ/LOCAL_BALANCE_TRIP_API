package live.lbtrip.domain.savedcourse.controller;

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
import live.lbtrip.domain.savedcourse.dto.response.TourProgressResponse;
import live.lbtrip.domain.savedcourse.dto.response.TourSummaryResponse;
import live.lbtrip.domain.savedcourse.model.SavedCourseStatus;
import live.lbtrip.domain.savedcourse.service.TourService;
import live.lbtrip.global.config.CorsProperties;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import live.lbtrip.support.fixture.AuthResponseFixture;
import live.lbtrip.support.fixture.TokenFixture;

@WebMvcTest(TourController.class)
@Import(TourControllerTest.TestCorsConfig.class)
class TourControllerTest {

    private static final Long SAVED_COURSE_ID = 1L;
    private static final Long PLACE_ID = 2L;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TourService tourService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMappingContext;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private AdminJwtTokenProvider adminJwtTokenProvider;

    @Nested
    class 투어_시작 {

        @Test
        void 투어를_시작하고_진행_상황을_응답한다() throws Exception {
            인증된_사용자();
            TourProgressResponse response = new TourProgressResponse(
                SAVED_COURSE_ID,
                SavedCourseStatus.TRAVELING,
                null,
                List.of()
            );
            when(tourService.startTour(AuthResponseFixture.USER_ID, SAVED_COURSE_ID))
                .thenReturn(response);

            mockMvc.perform(post("/saved-courses/{savedCourseId}/tour/start", SAVED_COURSE_ID)
                    .header("Authorization", "Bearer " + TokenFixture.ACCESS_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.savedCourseId").value(SAVED_COURSE_ID))
                .andExpect(jsonPath("$.data.status").value("TRAVELING"));
        }

        @Test
        void 이미_완주한_코스면_예외를_응답한다() throws Exception {
            인증된_사용자();
            when(tourService.startTour(AuthResponseFixture.USER_ID, SAVED_COURSE_ID))
                .thenThrow(BusinessException.of(ErrorCode.TOUR_ALREADY_COMPLETED));

            mockMvc.perform(post("/saved-courses/{savedCourseId}/tour/start", SAVED_COURSE_ID)
                    .header("Authorization", "Bearer " + TokenFixture.ACCESS_TOKEN))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("TOUR_ALREADY_COMPLETED"));
        }
    }

    @Nested
    class 장소_체크인 {

        @Test
        void 장소_방문을_체크인한다() throws Exception {
            인증된_사용자();

            mockMvc.perform(post(
                    "/saved-courses/{savedCourseId}/tour/places/{placeId}/check-in",
                    SAVED_COURSE_ID,
                    PLACE_ID
                )
                    .header("Authorization", "Bearer " + TokenFixture.ACCESS_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"));

            verify(tourService).checkIn(AuthResponseFixture.USER_ID, SAVED_COURSE_ID, PLACE_ID);
        }
    }

    @Nested
    class 투어_종료 {

        @Test
        void 투어를_종료하고_요약을_응답한다() throws Exception {
            인증된_사용자();
            when(tourService.endTour(AuthResponseFixture.USER_ID, SAVED_COURSE_ID))
                .thenReturn(new TourSummaryResponse(true, 2, 2, 60));

            mockMvc.perform(post("/saved-courses/{savedCourseId}/tour/end", SAVED_COURSE_ID)
                    .header("Authorization", "Bearer " + TokenFixture.ACCESS_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.completed").value(true))
                .andExpect(jsonPath("$.data.visitedPlaceCount").value(2))
                .andExpect(jsonPath("$.data.durationMinutes").value(60));
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
