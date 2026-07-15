package live.lbtrip.domain.admin.incentive.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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

import com.fasterxml.jackson.databind.ObjectMapper;

import live.lbtrip.domain.admin.auth.model.AdminJwtTokenSubject;
import live.lbtrip.domain.admin.auth.service.AdminJwtTokenProvider;
import live.lbtrip.domain.admin.incentive.dto.request.IncentiveRequest;
import live.lbtrip.domain.admin.incentive.service.IncentiveService;
import live.lbtrip.domain.auth.service.JwtTokenProvider;
import live.lbtrip.global.config.CorsProperties;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import live.lbtrip.support.fixture.AdminFixture;
import live.lbtrip.support.fixture.IncentiveRequestFixture;
import live.lbtrip.support.fixture.IncentiveResponseFixture;
import live.lbtrip.support.fixture.TokenFixture;

@WebMvcTest(IncentiveController.class)
@Import(IncentiveControllerTest.TestCorsConfig.class)
class IncentiveControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private IncentiveService incentiveService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private AdminJwtTokenProvider adminJwtTokenProvider;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMappingContext;

    @Nested
    class 등록 {

        @Test
        void 인센티브를_등록한다() throws Exception {
            인증된_어드민();
            when(incentiveService.createIncentive(any(IncentiveRequest.class)))
                .thenReturn(IncentiveResponseFixture.incentiveResponse());

            mockMvc.perform(post("/admin/incentives")
                    .header("Authorization", "Bearer " + TokenFixture.ADMIN_ACCESS_TOKEN)
                    .contentType(APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(IncentiveRequestFixture.incentiveRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.incentiveId").value(IncentiveResponseFixture.INCENTIVE_ID))
                .andExpect(jsonPath("$.data.title").value(IncentiveRequestFixture.TITLE))
                .andExpect(jsonPath("$.data.url").value(IncentiveRequestFixture.URL));
        }

        @Test
        void 제목이_비어있으면_예외를_응답한다() throws Exception {
            인증된_어드민();

            mockMvc.perform(post("/admin/incentives")
                    .header("Authorization", "Bearer " + TokenFixture.ADMIN_ACCESS_TOKEN)
                    .contentType(APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(new IncentiveRequest(
                        " ", IncentiveRequestFixture.URL, IncentiveRequestFixture.DESCRIPTION,
                        IncentiveRequestFixture.regions()))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("INVALID_INPUT_VALUE"));
        }

        @Test
        void 어드민_토큰이_없으면_예외를_응답한다() throws Exception {
            mockMvc.perform(post("/admin/incentives")
                    .contentType(APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(IncentiveRequestFixture.incentiveRequest())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("INVALID_ADMIN_ACCESS_TOKEN"));
        }

        @Test
        void 사용자_토큰으로_요청하면_예외를_응답한다() throws Exception {
            when(adminJwtTokenProvider.isValid(TokenFixture.ACCESS_TOKEN)).thenReturn(false);

            mockMvc.perform(post("/admin/incentives")
                    .header("Authorization", "Bearer " + TokenFixture.ACCESS_TOKEN)
                    .contentType(APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(IncentiveRequestFixture.incentiveRequest())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("INVALID_ADMIN_ACCESS_TOKEN"));
        }
    }

    @Nested
    class 목록_조회 {

        @Test
        void 인센티브_목록을_조회한다() throws Exception {
            인증된_어드민();
            when(incentiveService.getIncentives()).thenReturn(IncentiveResponseFixture.incentiveResponses());

            mockMvc.perform(get("/admin/incentives")
                    .header("Authorization", "Bearer " + TokenFixture.ADMIN_ACCESS_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data[0].incentiveId").value(IncentiveResponseFixture.INCENTIVE_ID))
                .andExpect(jsonPath("$.data[0].title").value(IncentiveRequestFixture.TITLE))
                .andExpect(jsonPath("$.data[0].url").value(IncentiveRequestFixture.URL));
        }
    }

    @Nested
    class 수정 {

        @Test
        void 인센티브를_수정한다() throws Exception {
            인증된_어드민();
            when(incentiveService.updateIncentive(eq(IncentiveResponseFixture.INCENTIVE_ID), any(IncentiveRequest.class)))
                .thenReturn(IncentiveResponseFixture.updatedIncentiveResponse());

            mockMvc.perform(put("/admin/incentives/{incentiveId}", IncentiveResponseFixture.INCENTIVE_ID)
                    .header("Authorization", "Bearer " + TokenFixture.ADMIN_ACCESS_TOKEN)
                    .contentType(APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(IncentiveRequestFixture.updatedIncentiveRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.title").value(IncentiveRequestFixture.UPDATED_TITLE))
                .andExpect(jsonPath("$.data.url").value(IncentiveRequestFixture.UPDATED_URL));
        }

        @Test
        void 인센티브가_존재하지_않으면_예외를_응답한다() throws Exception {
            인증된_어드민();
            when(incentiveService.updateIncentive(eq(IncentiveResponseFixture.INCENTIVE_ID), any(IncentiveRequest.class)))
                .thenThrow(BusinessException.of(ErrorCode.INCENTIVE_NOT_FOUND));

            mockMvc.perform(put("/admin/incentives/{incentiveId}", IncentiveResponseFixture.INCENTIVE_ID)
                    .header("Authorization", "Bearer " + TokenFixture.ADMIN_ACCESS_TOKEN)
                    .contentType(APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(IncentiveRequestFixture.updatedIncentiveRequest())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("INCENTIVE_NOT_FOUND"));
        }
    }

    @Nested
    class 삭제 {

        @Test
        void 인센티브를_삭제한다() throws Exception {
            인증된_어드민();

            mockMvc.perform(delete("/admin/incentives/{incentiveId}", IncentiveResponseFixture.INCENTIVE_ID)
                    .header("Authorization", "Bearer " + TokenFixture.ADMIN_ACCESS_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"));
        }
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
