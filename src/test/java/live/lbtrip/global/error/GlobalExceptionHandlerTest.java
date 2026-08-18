package live.lbtrip.global.error;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

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

        @Test
        void 복수_언어는_q값이_높은_지원_언어를_선택한다() throws Exception {
            mockMvc.perform(get("/not-found").header("Accept-Language", "ko;q=0.8, en;q=0.9"))
                .andExpect(header().string("Content-Language", "en"))
                .andExpect(jsonPath("$.error.message").value("The requested resource was not found."));
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
    }

    @TestConfiguration
    static class TestCorsConfig {

        @Bean
        CorsProperties corsProperties() {
            return new CorsProperties(List.of("http://localhost"));
        }
    }
}
