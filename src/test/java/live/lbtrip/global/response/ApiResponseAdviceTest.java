package live.lbtrip.global.response;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import live.lbtrip.domain.auth.controller.AuthController;
import live.lbtrip.domain.auth.dto.request.LoginRequest;
import live.lbtrip.domain.auth.dto.request.SignupRequest;
import live.lbtrip.domain.auth.dto.response.SignupResponse;
import live.lbtrip.domain.auth.model.JwtTokenSubject;
import live.lbtrip.domain.auth.service.AuthService;
import live.lbtrip.domain.auth.service.EmailVerificationService;
import live.lbtrip.domain.auth.service.JwtTokenProvider;
import live.lbtrip.domain.user.controller.UserController;
import live.lbtrip.domain.user.dto.response.EmailAvailabilityResponse;
import live.lbtrip.domain.user.model.UserStatus;
import live.lbtrip.domain.user.service.UserService;
import live.lbtrip.global.config.CorsProperties;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = {AuthController.class, UserController.class})
class ApiResponseAdviceTest {

	@TestConfiguration
	static class TestConfig {

		@Bean
		CorsProperties corsProperties() {
			return new CorsProperties(List.of("http://localhost:3000"));
		}
	}

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private AuthService authService;

	@MockitoBean
	private EmailVerificationService emailVerificationService;

	@MockitoBean
	private UserService userService;

	@MockitoBean
	private JwtTokenProvider jwtTokenProvider;

	@MockitoBean
	private JpaMetamodelMappingContext jpaMetamodelMappingContext;

	@Test
	void wrapsSuccessResponseBody() throws Exception {
		when(userService.checkEmailAvailability("new@example.com"))
			.thenReturn(EmailAvailabilityResponse.of(true));

		mockMvc.perform(get("/users/email-availability").param("email", "new@example.com"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.result").value("SUCCESS"))
			.andExpect(jsonPath("$.data.available").value(true))
			.andExpect(jsonPath("$.error").value(nullValue()));
	}

	@Test
	void keepsCreatedStatusAndWrapsResponseEntityBody() throws Exception {
		when(authService.signup(any(SignupRequest.class)))
			.thenReturn(new SignupResponse(
				1L,
				"user@example.com",
				UserStatus.PENDING_EMAIL_VERIFICATION,
				86_400L
			));

		mockMvc.perform(post("/auth/signup")
				.contentType(APPLICATION_JSON)
				.content("""
					{
					  "name": "홍길동",
					  "email": "user@example.com",
					  "password": "password1",
					  "passwordConfirm": "password1",
					  "birthDate": "1999-01-01",
					  "gender": "NOT_SPECIFIED",
					  "termsAgreed": true,
					  "privacyAgreed": true,
					  "marketingAgreed": false
					}
					"""))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.result").value("SUCCESS"))
			.andExpect(jsonPath("$.data.userId").value(1))
			.andExpect(jsonPath("$.data.email").value("user@example.com"))
			.andExpect(jsonPath("$.error").value(nullValue()));
	}

	@Test
	void wrapsVoidSuccessResponse() throws Exception {
		when(jwtTokenProvider.isValid("access-token")).thenReturn(true);
		when(jwtTokenProvider.parseSubject("access-token")).thenReturn(new JwtTokenSubject(1L));

		mockMvc.perform(post("/auth/logout").header("Authorization", "Bearer access-token"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.result").value("SUCCESS"))
			.andExpect(jsonPath("$.data").value(nullValue()))
			.andExpect(jsonPath("$.error").value(nullValue()));

		verify(authService).logout(1L);
	}

	@Test
	void doesNotWrapErrorResponseAgain() throws Exception {
		when(authService.login(any(LoginRequest.class)))
			.thenThrow(BusinessException.of(ErrorCode.INVALID_LOGIN_CREDENTIALS));

		mockMvc.perform(post("/auth/login")
				.contentType(APPLICATION_JSON)
				.content("""
					{
					  "email": "user@example.com",
					  "password": "password1"
					}
					"""))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.result").value("ERROR"))
			.andExpect(jsonPath("$.data").value(nullValue()))
			.andExpect(jsonPath("$.error.code").value("INVALID_LOGIN_CREDENTIALS"))
			.andExpect(jsonPath("$.error.data").value(nullValue()));
	}

	@Test
	void keepsValidationErrorResponseShape() throws Exception {
		mockMvc.perform(post("/auth/login")
				.contentType(APPLICATION_JSON)
				.content("""
					{
					  "email": "not-email",
					  "password": ""
					}
					"""))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.result").value("ERROR"))
			.andExpect(jsonPath("$.data").value(nullValue()))
			.andExpect(jsonPath("$.error.code").value("INVALID_INPUT_VALUE"))
			.andExpect(jsonPath("$.error.data").isArray());
	}
}
