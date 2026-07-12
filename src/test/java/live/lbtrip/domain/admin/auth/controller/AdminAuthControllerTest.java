package live.lbtrip.domain.admin.auth.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
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

import com.fasterxml.jackson.databind.ObjectMapper;

import live.lbtrip.domain.admin.auth.dto.request.AdminLoginRequest;
import live.lbtrip.domain.admin.auth.dto.response.AdminTokenResponse;
import live.lbtrip.domain.admin.auth.service.AdminAuthService;
import live.lbtrip.domain.admin.auth.service.AdminJwtTokenProvider;
import live.lbtrip.domain.auth.service.JwtTokenProvider;
import live.lbtrip.global.config.CorsProperties;
import live.lbtrip.support.fixture.AdminFixture;
import live.lbtrip.support.fixture.TokenFixture;

@WebMvcTest(AdminAuthController.class)
@Import(AdminAuthControllerTest.TestCorsConfig.class)
class AdminAuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private AdminAuthService adminAuthService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private AdminJwtTokenProvider adminJwtTokenProvider;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMappingContext;

    @Nested
    class 로그인 {

        @Test
        void 어드민_로그인에_성공하면_액세스_토큰을_응답한다() throws Exception {
            when(adminAuthService.login(any(AdminLoginRequest.class)))
                .thenReturn(AdminTokenResponse.of(TokenFixture.ADMIN_ACCESS_TOKEN));

            mockMvc.perform(post("/admin/auth/login")
                    .contentType(APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(
                        new AdminLoginRequest(AdminFixture.EMAIL, AdminFixture.PASSWORD))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.accessToken").value(TokenFixture.ADMIN_ACCESS_TOKEN));
        }

        @Test
        void 이메일_형식이_올바르지_않으면_예외를_응답한다() throws Exception {
            mockMvc.perform(post("/admin/auth/login")
                    .contentType(APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(
                        new AdminLoginRequest("invalid-email", AdminFixture.PASSWORD))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("INVALID_INPUT_VALUE"));
        }
    }

    @TestConfiguration
    static class TestCorsConfig {

        @Bean
        CorsProperties corsProperties() {
            return new CorsProperties(List.of("http://localhost"));
        }
    }
}
