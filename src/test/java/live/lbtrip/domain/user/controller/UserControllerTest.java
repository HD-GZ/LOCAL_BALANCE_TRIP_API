package live.lbtrip.domain.user.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

import live.lbtrip.domain.auth.service.JwtTokenProvider;
import live.lbtrip.domain.user.dto.response.EmailAvailabilityResponse;
import live.lbtrip.domain.user.service.UserService;
import live.lbtrip.global.config.CorsProperties;
import live.lbtrip.support.fixture.UserFixture;

@WebMvcTest(UserController.class)
@Import(UserControllerTest.TestCorsConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMappingContext;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

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

    @TestConfiguration
    static class TestCorsConfig {

        @Bean
        CorsProperties corsProperties() {
            return new CorsProperties(List.of("http://localhost"));
        }
    }
}
