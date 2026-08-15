package live.lbtrip.domain.terms.controller;

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

import live.lbtrip.admin.auth.service.AdminJwtTokenProvider;
import live.lbtrip.domain.auth.service.JwtTokenProvider;
import live.lbtrip.domain.terms.service.TermsService;
import live.lbtrip.global.config.CorsProperties;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import live.lbtrip.support.fixture.TermsFixture;

@WebMvcTest(TermsController.class)
@Import(TermsControllerTest.TestCorsConfig.class)
class TermsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TermsService termsService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private AdminJwtTokenProvider adminJwtTokenProvider;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMappingContext;

    @Nested
    class 조회 {

        @Test
        void 약관_전문을_조회한다() throws Exception {
            when(termsService.getTerms(TermsFixture.TYPE_PATH)).thenReturn(TermsFixture.termsResponse());

            mockMvc.perform(get("/terms/{type}", TermsFixture.TYPE_PATH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.type").value(TermsFixture.TYPE.name()))
                .andExpect(jsonPath("$.data.title").value(TermsFixture.TITLE))
                .andExpect(jsonPath("$.data.version").value(TermsFixture.VERSION))
                .andExpect(jsonPath("$.data.effectiveDate").value(TermsFixture.EFFECTIVE_DATE.toString()))
                .andExpect(jsonPath("$.data.content").value(TermsFixture.CONTENT));
        }

        @Test
        void 인증_없이_조회할_수_있다() throws Exception {
            when(termsService.getTerms(TermsFixture.TYPE_PATH)).thenReturn(TermsFixture.termsResponse());

            mockMvc.perform(get("/terms/{type}", TermsFixture.TYPE_PATH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"));
        }

        @Test
        void 시행_중인_약관이_없으면_예외를_응답한다() throws Exception {
            when(termsService.getTerms(TermsFixture.TYPE_PATH))
                .thenThrow(BusinessException.of(ErrorCode.TERMS_NOT_FOUND));

            mockMvc.perform(get("/terms/{type}", TermsFixture.TYPE_PATH))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("TERMS_NOT_FOUND"));
        }

        @Test
        void 지원하지_않는_약관_종류면_예외를_응답한다() throws Exception {
            when(termsService.getTerms(TermsFixture.UNSUPPORTED_TYPE_PATH))
                .thenThrow(BusinessException.of(ErrorCode.TERMS_NOT_FOUND));

            mockMvc.perform(get("/terms/{type}", TermsFixture.UNSUPPORTED_TYPE_PATH))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("TERMS_NOT_FOUND"));
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
