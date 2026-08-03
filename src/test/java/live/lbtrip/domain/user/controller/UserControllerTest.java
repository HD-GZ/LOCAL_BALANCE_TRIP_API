package live.lbtrip.domain.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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

import live.lbtrip.domain.auth.model.JwtTokenSubject;
import live.lbtrip.admin.auth.service.AdminJwtTokenProvider;
import live.lbtrip.domain.auth.service.JwtTokenProvider;
import live.lbtrip.domain.user.dto.request.UserUpdateRequest;
import live.lbtrip.domain.user.dto.response.EmailAvailabilityResponse;
import live.lbtrip.domain.user.service.UserService;
import live.lbtrip.global.config.CorsProperties;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import live.lbtrip.support.fixture.AuthResponseFixture;
import live.lbtrip.support.fixture.TokenFixture;
import live.lbtrip.support.fixture.UserFixture;
import live.lbtrip.support.fixture.UserRequestFixture;
import live.lbtrip.support.fixture.UserResponseFixture;

@WebMvcTest(UserController.class)
@Import(UserControllerTest.TestCorsConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMappingContext;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private AdminJwtTokenProvider adminJwtTokenProvider;

    @Nested
    class 이메일_사용_가능_여부 {

        @Test
        void 이메일_사용_가능_여부를_조회한다() throws Exception {
            when(userService.checkEmailAvailability(UserFixture.EMAIL))
                .thenReturn(EmailAvailabilityResponse.of(true));

            mockMvc.perform(get("/users/email-availability")
                    .param("email", UserFixture.EMAIL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.available").value(true));
        }

        @Test
        void 이메일_형식이_올바르지_않으면_예외를_응답한다() throws Exception {
            mockMvc.perform(get("/users/email-availability")
                    .param("email", "invalid-email"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("INVALID_INPUT_VALUE"));
        }
    }

    @Nested
    class 내_정보_조회 {

        @Test
        void 사용자_정보를_응답한다() throws Exception {
            인증된_사용자();
            when(userService.getUser(AuthResponseFixture.USER_ID))
                .thenReturn(UserResponseFixture.userResponse());

            mockMvc.perform(get("/users/me")
                    .header("Authorization", "Bearer " + TokenFixture.ACCESS_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.userId").value(AuthResponseFixture.USER_ID))
                .andExpect(jsonPath("$.data.name").value(UserFixture.NAME))
                .andExpect(jsonPath("$.data.email").value(UserFixture.EMAIL))
                .andExpect(jsonPath("$.data.gender").value(UserFixture.GENDER.name()))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andExpect(jsonPath("$.data.marketingAgreed").value(UserResponseFixture.MARKETING_AGREED));
        }

        @Test
        void 인증_토큰이_없으면_예외를_응답한다() throws Exception {
            mockMvc.perform(get("/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("INVALID_ACCESS_TOKEN"));
        }

        @Test
        void 사용자가_존재하지_않으면_예외를_응답한다() throws Exception {
            인증된_사용자();
            when(userService.getUser(AuthResponseFixture.USER_ID))
                .thenThrow(BusinessException.of(ErrorCode.USER_NOT_FOUND));

            mockMvc.perform(get("/users/me")
                    .header("Authorization", "Bearer " + TokenFixture.ACCESS_TOKEN))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("USER_NOT_FOUND"));
        }
    }

    @Nested
    class 내_정보_수정 {

        @Test
        void 회원_정보를_수정한다() throws Exception {
            인증된_사용자();
            when(userService.updateUser(eq(AuthResponseFixture.USER_ID), any(UserUpdateRequest.class)))
                .thenReturn(UserResponseFixture.updatedUserResponse());

            mockMvc.perform(patch("/users/me")
                    .header("Authorization", "Bearer " + TokenFixture.ACCESS_TOKEN)
                    .contentType(APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(UserRequestFixture.userUpdateRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.userId").value(AuthResponseFixture.USER_ID))
                .andExpect(jsonPath("$.data.name").value(UserRequestFixture.NEW_NAME))
                .andExpect(jsonPath("$.data.birthDate").value(UserRequestFixture.NEW_BIRTH_DATE.toString()))
                .andExpect(jsonPath("$.data.gender").value(UserRequestFixture.NEW_GENDER.name()))
                .andExpect(jsonPath("$.data.email").value(UserFixture.EMAIL));
        }

        @Test
        void 이름이_없으면_예외를_응답한다() throws Exception {
            인증된_사용자();
            UserUpdateRequest request = new UserUpdateRequest(
                null,
                UserRequestFixture.NEW_BIRTH_DATE,
                UserRequestFixture.NEW_GENDER,
                null,
                null
            );

            mockMvc.perform(patch("/users/me")
                    .header("Authorization", "Bearer " + TokenFixture.ACCESS_TOKEN)
                    .contentType(APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("INVALID_INPUT_VALUE"));
        }

        @Test
        void 비밀번호_확인이_일치하지_않으면_예외를_응답한다() throws Exception {
            인증된_사용자();
            UserUpdateRequest request = new UserUpdateRequest(
                UserRequestFixture.NEW_NAME,
                UserRequestFixture.NEW_BIRTH_DATE,
                UserRequestFixture.NEW_GENDER,
                UserRequestFixture.NEW_PASSWORD,
                "different1"
            );

            mockMvc.perform(patch("/users/me")
                    .header("Authorization", "Bearer " + TokenFixture.ACCESS_TOKEN)
                    .contentType(APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("INVALID_INPUT_VALUE"));
        }

        @Test
        void 인증_토큰이_없으면_예외를_응답한다() throws Exception {
            mockMvc.perform(patch("/users/me")
                    .contentType(APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(UserRequestFixture.userUpdateRequest())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("INVALID_ACCESS_TOKEN"));
        }
    }

    @Nested
    class 회원탈퇴 {

        @Test
        void 회원탈퇴_요청을_처리한다() throws Exception {
            인증된_사용자();
            doNothing().when(userService).withdraw(AuthResponseFixture.USER_ID);

            mockMvc.perform(delete("/users/me")
                    .header("Authorization", "Bearer " + TokenFixture.ACCESS_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"));
        }

        @Test
        void 이미_탈퇴한_회원이면_예외를_응답한다() throws Exception {
            인증된_사용자();
            doThrow(BusinessException.of(ErrorCode.USER_WITHDRAWN))
                .when(userService).withdraw(AuthResponseFixture.USER_ID);

            mockMvc.perform(delete("/users/me")
                    .header("Authorization", "Bearer " + TokenFixture.ACCESS_TOKEN))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("USER_WITHDRAWN"));
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
