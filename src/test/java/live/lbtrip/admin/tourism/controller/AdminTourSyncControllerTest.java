package live.lbtrip.admin.tourism.controller;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import live.lbtrip.admin.auth.model.AdminJwtTokenSubject;
import live.lbtrip.admin.auth.service.AdminJwtTokenProvider;
import live.lbtrip.admin.tourism.service.AdminTourSyncService;
import live.lbtrip.domain.auth.service.JwtTokenProvider;
import live.lbtrip.domain.tourism.model.enums.TourSyncStep;
import live.lbtrip.global.config.CorsProperties;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import live.lbtrip.support.config.I18nTestConfig;
import live.lbtrip.support.fixture.AdminFixture;
import live.lbtrip.support.fixture.TokenFixture;

@WebMvcTest(AdminTourSyncController.class)
@Import({AdminTourSyncControllerTest.TestCorsConfig.class, I18nTestConfig.class})
class AdminTourSyncControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminTourSyncService adminTourSyncService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private AdminJwtTokenProvider adminJwtTokenProvider;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMappingContext;

    @Test
    void 관광_데이터_동기화를_시작한다() throws Exception {
        인증된_어드민();

        mockMvc.perform(post("/admin/tour-sync")
                .header("Authorization", "Bearer " + TokenFixture.ADMIN_ACCESS_TOKEN))
            .andExpect(status().isAccepted())
            .andExpect(jsonPath("$.result").value("SUCCESS"))
            .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void 이미_실행_중이면_예외를_응답한다() throws Exception {
        인증된_어드민();
        doThrow(BusinessException.of(ErrorCode.TOUR_SYNC_IN_PROGRESS))
            .when(adminTourSyncService).triggerSync();

        mockMvc.perform(post("/admin/tour-sync")
                .header("Authorization", "Bearer " + TokenFixture.ADMIN_ACCESS_TOKEN))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.result").value("ERROR"))
            .andExpect(jsonPath("$.error.code").value("TOUR_SYNC_IN_PROGRESS"));
    }

    @Test
    void 지정한_단계만_동기화를_시작한다() throws Exception {
        인증된_어드민();

        mockMvc.perform(post("/admin/tour-sync/OVERVIEWS")
                .header("Authorization", "Bearer " + TokenFixture.ADMIN_ACCESS_TOKEN))
            .andExpect(status().isAccepted())
            .andExpect(jsonPath("$.result").value("SUCCESS"));

        verify(adminTourSyncService).triggerSync(TourSyncStep.OVERVIEWS);
    }

    @Test
    void 존재하지_않는_단계를_요청하면_예외를_응답한다() throws Exception {
        인증된_어드민();

        mockMvc.perform(post("/admin/tour-sync/UNKNOWN_STEP")
                .header("Authorization", "Bearer " + TokenFixture.ADMIN_ACCESS_TOKEN))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.result").value("ERROR"))
            .andExpect(jsonPath("$.error.code").value("INVALID_INPUT_VALUE"));
    }

    @Test
    void 단계_실행도_이미_실행_중이면_예외를_응답한다() throws Exception {
        인증된_어드민();
        doThrow(BusinessException.of(ErrorCode.TOUR_SYNC_IN_PROGRESS))
            .when(adminTourSyncService).triggerSync(TourSyncStep.REGIONS);

        mockMvc.perform(post("/admin/tour-sync/REGIONS")
                .header("Authorization", "Bearer " + TokenFixture.ADMIN_ACCESS_TOKEN))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.error.code").value("TOUR_SYNC_IN_PROGRESS"));
    }

    @Test
    void 어드민_토큰이_없으면_예외를_응답한다() throws Exception {
        mockMvc.perform(post("/admin/tour-sync"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.result").value("ERROR"))
            .andExpect(jsonPath("$.error.code").value("INVALID_ADMIN_ACCESS_TOKEN"));
    }

    private void 인증된_어드민() {
        when(adminJwtTokenProvider.isValid(TokenFixture.ADMIN_ACCESS_TOKEN)).thenReturn(true);
        when(adminJwtTokenProvider.parseSubject(TokenFixture.ADMIN_ACCESS_TOKEN))
            .thenReturn(AdminJwtTokenSubject.of(AdminFixture.ADMIN_ID));
    }

    @TestConfiguration
    static class TestCorsConfig {

        @Bean
        CorsProperties corsProperties() {
            return new CorsProperties(List.of("http://localhost"));
        }
    }
}
