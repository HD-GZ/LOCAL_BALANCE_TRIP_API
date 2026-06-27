package live.lbtrip.global.swagger;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class OpenApiDocumentationTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void documentsSuccessResponseAsApiResponseWrapper() throws Exception {
		mockMvc.perform(get("/v3/api-docs"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.paths['/auth/signup'].post.responses['201'].description").value("회원가입 성공"))
			.andExpect(jsonPath("$.paths['/auth/signup'].post.responses['201'].content['application/json'].schema.properties.result.example")
				.value("SUCCESS"))
			.andExpect(jsonPath("$.paths['/auth/signup'].post.responses['201'].content['application/json'].schema.properties.data")
				.exists())
			.andExpect(jsonPath("$.paths['/auth/signup'].post.responses['201'].content['application/json'].schema.properties.error")
				.exists());
	}

	@Test
	void documentsErrorCodeResponsesWithExamples() throws Exception {
		mockMvc.perform(get("/v3/api-docs"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.paths['/auth/login'].post.responses['401'].content['application/json'].examples.INVALID_LOGIN_CREDENTIALS.value.error.code")
				.value("INVALID_LOGIN_CREDENTIALS"))
			.andExpect(jsonPath("$.paths['/auth/login'].post.responses['403'].content['application/json'].examples.EMAIL_NOT_VERIFIED.value.error.code")
				.value("EMAIL_NOT_VERIFIED"));
	}

	@Test
	void documentsValidationErrorExample() throws Exception {
		mockMvc.perform(get("/v3/api-docs"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.paths['/users/email-availability'].get.responses['400'].content['application/json'].examples.INVALID_INPUT_VALUE.value.error.code")
				.value("INVALID_INPUT_VALUE"))
			.andExpect(jsonPath("$.paths['/users/email-availability'].get.responses['400'].content['application/json'].examples.INVALID_INPUT_VALUE.value.error.data[0].field")
				.value("email"));
	}
}
