package live.lbtrip.global.error;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.util.List;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import live.lbtrip.admin.auth.service.AdminJwtTokenProvider;
import live.lbtrip.domain.auth.service.JwtTokenProvider;
import live.lbtrip.global.config.CorsProperties;
import live.lbtrip.support.config.I18nTestConfig;

@WebMvcTest(GlobalExceptionHandlerTest.TestController.class)
@Import({GlobalExceptionHandlerTest.TestCorsConfig.class, I18nTestConfig.class})
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMappingContext;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private AdminJwtTokenProvider adminJwtTokenProvider;

    @Nested
    class 존재하지_않는_리소스 {

        @Test
        void 공통_응답_형식으로_404를_응답한다() throws Exception {
            mockMvc.perform(get("/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.error.code").value("RESOURCE_NOT_FOUND"));
        }
    }

    @Nested
    class 이미지_크기_초과 {

        @Test
        void 공통_응답_형식으로_413을_응답한다() throws Exception {
            mockMvc.perform(get("/too-large"))
                .andExpect(status().isContentTooLarge())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.error.code").value("IMAGE_SIZE_EXCEEDED"));
        }
    }

    @Nested
    class 로케일별_에러_메시지 {

        @Test
        void 헤더가_없으면_한국어_메시지와_Content_Language_ko를_응답한다() throws Exception {
            mockMvc.perform(get("/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(header().string("Content-Language", "ko"))
                .andExpect(jsonPath("$.error.code").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.error.message").value("요청한 리소스를 찾을 수 없습니다."));
        }

        @Test
        void Accept_Language_en이면_영어_메시지와_Content_Language_en을_응답한다() throws Exception {
            mockMvc.perform(get("/not-found").header("Accept-Language", "en"))
                .andExpect(status().isNotFound())
                .andExpect(header().string("Content-Language", "en"))
                .andExpect(jsonPath("$.error.message").value("The requested resource was not found."));
        }

        @Test
        void en_US처럼_지역_변형도_영어로_응답한다() throws Exception {
            mockMvc.perform(get("/not-found").header("Accept-Language", "en-US,en;q=0.9"))
                .andExpect(header().string("Content-Language", "en"))
                .andExpect(jsonPath("$.error.message").value("The requested resource was not found."));
        }

        @Test
        void 미지원_로케일이면_한국어로_폴백한다() throws Exception {
            mockMvc.perform(get("/not-found").header("Accept-Language", "ja"))
                .andExpect(header().string("Content-Language", "ko"))
                .andExpect(jsonPath("$.error.message").value("요청한 리소스를 찾을 수 없습니다."));
        }
    }

    @Nested
    class 로케일별_검증_메시지 {

        @Test
        void 헤더가_없으면_한국어_검증_메시지를_응답한다() throws Exception {
            mockMvc.perform(post("/validate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"email\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_INPUT_VALUE"))
                .andExpect(jsonPath("$.error.message").value("입력값이 올바르지 않습니다."))
                .andExpect(jsonPath("$.error.data[0].field").value("email"))
                .andExpect(jsonPath("$.error.data[0].message").value("이메일은 필수입니다."));
        }

        @Test
        void Accept_Language_en이면_영어_검증_메시지를_응답한다() throws Exception {
            mockMvc.perform(post("/validate")
                    .header("Accept-Language", "en")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"email\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.message").value("Invalid input value."))
                .andExpect(jsonPath("$.error.data[0].message").value("Email is required."));
        }

        @Test
        void 애노테이션_속성이_메시지에_보간된다() throws Exception {
            mockMvc.perform(post("/validate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"email\":\"a@b.com\",\"name\":\"012345678901234567890\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.data[0].field").value("name"))
                .andExpect(jsonPath("$.error.data[0].message").value("이름은 20자 이하여야 합니다."));
        }

        @Test
        void 영어_메시지에도_애노테이션_속성이_보간된다() throws Exception {
            mockMvc.perform(post("/validate")
                    .header("Accept-Language", "en")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"email\":\"a@b.com\",\"name\":\"012345678901234567890\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.data[0].field").value("name"))
                .andExpect(jsonPath("$.error.data[0].message").value("Name must be at most 20 characters."));
        }
    }

    @RestController
    static class TestController {

        @GetMapping("/test")
        void test() {
        }

        @GetMapping("/too-large")
        void tooLarge() {
            throw new MaxUploadSizeExceededException(10L * 1024 * 1024);
        }

        @PostMapping("/validate")
        void validate(@RequestBody @Valid ValidationRequest request) {
        }

        record ValidationRequest(
            @NotBlank(message = "{validation.email.required}")
            @Email(message = "{validation.email.format}")
            String email,
            @Size(max = 20, message = "{validation.name.size}")
            String name
        ) {
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
