package live.lbtrip.domain.auth.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
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

import live.lbtrip.admin.auth.service.AdminJwtTokenProvider;
import live.lbtrip.domain.auth.dto.request.PasswordResetCodeRequest;
import live.lbtrip.domain.auth.dto.request.PasswordResetConfirmRequest;
import live.lbtrip.domain.auth.dto.request.PasswordResetRequest;
import live.lbtrip.domain.auth.dto.response.PasswordResetCodeResponse;
import live.lbtrip.domain.auth.dto.response.PasswordResetTokenResponse;
import live.lbtrip.domain.auth.service.JwtTokenProvider;
import live.lbtrip.domain.auth.service.PasswordResetService;
import live.lbtrip.global.config.CorsProperties;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import live.lbtrip.support.fixture.PasswordResetFixture;
import live.lbtrip.support.fixture.UserFixture;

@WebMvcTest(PasswordResetController.class)
@Import(PasswordResetControllerTest.TestCorsConfig.class)
class PasswordResetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private PasswordResetService passwordResetService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private AdminJwtTokenProvider adminJwtTokenProvider;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMappingContext;

    @Nested
    class 인증_코드_요청 {

        @Test
        void 인증_코드_발송_요청을_처리한다() throws Exception {
            when(passwordResetService.request(any(PasswordResetCodeRequest.class)))
                .thenReturn(PasswordResetCodeResponse.of(600L));

            mockMvc.perform(post("/auth/password-reset/request")
                    .contentType(APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(new PasswordResetCodeRequest(UserFixture.EMAIL))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.expiresIn").value(600));
        }

        @Test
        void 이메일_형식이_올바르지_않으면_예외를_응답한다() throws Exception {
            mockMvc.perform(post("/auth/password-reset/request")
                    .contentType(APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(new PasswordResetCodeRequest("invalid-email"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("INVALID_INPUT_VALUE"));
        }

        @Test
        void 가입되지_않은_이메일이면_예외를_응답한다() throws Exception {
            doThrow(BusinessException.of(ErrorCode.USER_NOT_FOUND))
                .when(passwordResetService).request(any(PasswordResetCodeRequest.class));

            mockMvc.perform(post("/auth/password-reset/request")
                    .contentType(APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(new PasswordResetCodeRequest(UserFixture.EMAIL))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("USER_NOT_FOUND"));
        }
    }

    @Nested
    class 인증_코드_확인 {

        @Test
        void 인증_코드_확인_요청을_처리한다() throws Exception {
            when(passwordResetService.confirm(any(PasswordResetConfirmRequest.class)))
                .thenReturn(PasswordResetTokenResponse.of(PasswordResetFixture.RESET_TOKEN, 600L));

            mockMvc.perform(post("/auth/password-reset/confirm")
                    .contentType(APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(
                        new PasswordResetConfirmRequest(UserFixture.EMAIL, PasswordResetFixture.CODE))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.resetToken").value(PasswordResetFixture.RESET_TOKEN));
        }

        @Test
        void 만료된_코드면_예외를_응답한다() throws Exception {
            doThrow(BusinessException.of(ErrorCode.PASSWORD_RESET_CODE_EXPIRED))
                .when(passwordResetService).confirm(any(PasswordResetConfirmRequest.class));

            mockMvc.perform(post("/auth/password-reset/confirm")
                    .contentType(APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(
                        new PasswordResetConfirmRequest(UserFixture.EMAIL, PasswordResetFixture.CODE))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("PASSWORD_RESET_CODE_EXPIRED"));
        }
    }

    @Nested
    class 비밀번호_재설정 {

        @Test
        void 비밀번호_재설정_요청을_처리한다() throws Exception {
            mockMvc.perform(post("/auth/password-reset")
                    .contentType(APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(
                        new PasswordResetRequest(PasswordResetFixture.RESET_TOKEN, PasswordResetFixture.NEW_PASSWORD))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"));
        }

        @Test
        void 새_비밀번호가_규칙에_맞지_않으면_예외를_응답한다() throws Exception {
            mockMvc.perform(post("/auth/password-reset")
                    .contentType(APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(
                        new PasswordResetRequest(PasswordResetFixture.RESET_TOKEN, "short"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("INVALID_INPUT_VALUE"));
        }

        @Test
        void 이미_사용된_리셋_토큰이면_예외를_응답한다() throws Exception {
            doThrow(BusinessException.of(ErrorCode.PASSWORD_RESET_TOKEN_USED))
                .when(passwordResetService).reset(any(PasswordResetRequest.class));

            mockMvc.perform(post("/auth/password-reset")
                    .contentType(APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(
                        new PasswordResetRequest(PasswordResetFixture.RESET_TOKEN, PasswordResetFixture.NEW_PASSWORD))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("PASSWORD_RESET_TOKEN_USED"));
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
