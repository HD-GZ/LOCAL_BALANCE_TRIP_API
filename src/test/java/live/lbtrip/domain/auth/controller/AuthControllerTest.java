package live.lbtrip.domain.auth.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
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

import live.lbtrip.domain.auth.dto.request.EmailVerificationConfirmRequest;
import live.lbtrip.domain.auth.dto.request.EmailVerificationResendRequest;
import live.lbtrip.domain.auth.dto.request.LoginRequest;
import live.lbtrip.domain.auth.dto.request.SignupRequest;
import live.lbtrip.domain.auth.dto.request.TokenRefreshRequest;
import live.lbtrip.domain.auth.service.AuthService;
import live.lbtrip.domain.auth.service.EmailVerificationService;
import live.lbtrip.domain.admin.auth.service.AdminJwtTokenProvider;
import live.lbtrip.domain.auth.service.JwtTokenProvider;
import live.lbtrip.global.config.CorsProperties;
import live.lbtrip.support.fixture.AuthRequestFixture;
import live.lbtrip.support.fixture.AuthResponseFixture;
import live.lbtrip.support.fixture.TokenFixture;

@WebMvcTest(AuthController.class)
@Import(AuthControllerTest.TestCorsConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private EmailVerificationService emailVerificationService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private AdminJwtTokenProvider adminJwtTokenProvider;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMappingContext;

    @Nested
    class 회원가입 {

        @Test
        void 회원가입_요청을_처리한다() throws Exception {
            when(authService.signup(any(SignupRequest.class))).thenReturn(AuthResponseFixture.signupResponse());

            mockMvc.perform(post("/auth/signup")
                    .contentType(APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(AuthRequestFixture.signupRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.email").value("user@example.com"))
                .andExpect(jsonPath("$.data.status").value("PENDING_EMAIL_VERIFICATION"));
        }

        @Test
        void 회원가입_요청값이_올바르지_않으면_예외를_응답한다() throws Exception {
            String request = """
                {
                  "name": "",
                  "email": "invalid-email",
                  "password": "password1",
                  "passwordConfirm": "password1",
                  "birthDate": "1999-01-01",
                  "gender": "NOT_SPECIFIED",
                  "termsAgreed": true,
                  "privacyAgreed": true,
                  "marketingAgreed": false
                }
                """;

            mockMvc.perform(post("/auth/signup")
                    .contentType(APPLICATION_JSON)
                    .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("INVALID_INPUT_VALUE"));
        }
    }

    @Nested
    class 로그인 {

        @Test
        void 로그인_요청을_처리한다() throws Exception {
            when(authService.login(any(LoginRequest.class))).thenReturn(AuthResponseFixture.loginResponse());

            mockMvc.perform(post("/auth/login")
                    .contentType(APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(AuthRequestFixture.loginRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.accessToken").value(TokenFixture.ACCESS_TOKEN))
                .andExpect(jsonPath("$.data.refreshToken").value(TokenFixture.REFRESH_TOKEN));
        }
    }

    @Nested
    class 토큰 {

        @Test
        void 토큰을_갱신한다() throws Exception {
            when(authService.refreshToken(any(TokenRefreshRequest.class))).thenReturn(AuthResponseFixture.tokenResponse());

            mockMvc.perform(post("/auth/refresh")
                    .contentType(APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(AuthRequestFixture.tokenRefreshRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.accessToken").value(TokenFixture.NEW_ACCESS_TOKEN))
                .andExpect(jsonPath("$.data.refreshToken").value(TokenFixture.REFRESH_TOKEN));
        }

        @Test
        void 로그아웃_요청을_처리한다() throws Exception {
            when(jwtTokenProvider.isValid(TokenFixture.ACCESS_TOKEN)).thenReturn(true);
            when(jwtTokenProvider.parseSubject(TokenFixture.ACCESS_TOKEN))
                .thenReturn(live.lbtrip.domain.auth.model.JwtTokenSubject.of(AuthResponseFixture.USER_ID));
            doNothing().when(authService).logout(AuthResponseFixture.USER_ID);

            mockMvc.perform(post("/auth/logout")
                    .header("Authorization", "Bearer " + TokenFixture.ACCESS_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"));
        }
    }

    @Nested
    class 이메일_인증 {

        @Test
        void 이메일_인증_코드를_확인한다() throws Exception {
            when(emailVerificationService.confirm(any(EmailVerificationConfirmRequest.class)))
                .thenReturn(AuthResponseFixture.emailVerificationResponse());

            mockMvc.perform(post("/auth/email-verifications/confirm")
                    .contentType(APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(AuthRequestFixture.emailVerificationConfirmRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.email").value("user@example.com"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
        }

        @Test
        void 이메일_인증_코드를_재발송한다() throws Exception {
            when(emailVerificationService.resend(any(EmailVerificationResendRequest.class)))
                .thenReturn(AuthResponseFixture.emailVerificationResponse());

            mockMvc.perform(post("/auth/email-verifications/resend")
                    .contentType(APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(AuthRequestFixture.emailVerificationResendRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.email").value("user@example.com"));
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
