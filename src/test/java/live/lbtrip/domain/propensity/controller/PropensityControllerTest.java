package live.lbtrip.domain.propensity.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

import live.lbtrip.domain.auth.model.JwtTokenSubject;
import live.lbtrip.domain.auth.service.JwtTokenProvider;
import live.lbtrip.domain.propensity.dto.request.PropensityRequest;
import live.lbtrip.domain.propensity.service.PropensityService;
import live.lbtrip.global.config.CorsProperties;
import live.lbtrip.support.fixture.AuthResponseFixture;
import live.lbtrip.support.fixture.PropensityRequestFixture;
import live.lbtrip.support.fixture.PropensityResponseFixture;
import live.lbtrip.support.fixture.TokenFixture;

@WebMvcTest(PropensityController.class)
@Import(PropensityControllerTest.TestCorsConfig.class)
class PropensityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private PropensityService propensityService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMappingContext;

    @Nested
    class 등록 {

        @Test
        void 취향_진단_결과를_등록한다() throws Exception {
            인증된_사용자();
            when(propensityService.setPropensity(any(Long.class), any(PropensityRequest.class)))
                .thenReturn(PropensityResponseFixture.propensityResponse());

            mockMvc.perform(post("/propensity")
                    .header("Authorization", "Bearer " + TokenFixture.ACCESS_TOKEN)
                    .contentType(APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(PropensityRequestFixture.propensityRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.result.type").value(PropensityResponseFixture.TYPE))
                .andExpect(jsonPath("$.data.locality").value(PropensityRequestFixture.LOCALITY))
                .andExpect(jsonPath("$.data.frugality").value(PropensityRequestFixture.FRUGALITY))
                .andExpect(jsonPath("$.data.flexibility").value(PropensityRequestFixture.FLEXIBILITY))
                .andExpect(jsonPath("$.data.experientiality").value(PropensityRequestFixture.EXPERIENTIALITY))
                .andExpect(jsonPath("$.data.vitality").value(PropensityRequestFixture.VITALITY))
                .andExpect(jsonPath("$.data.sociality").value(PropensityRequestFixture.SOCIALITY));
        }

        @Test
        void 점수가_범위를_벗어나면_예외를_응답한다() throws Exception {
            인증된_사용자();
            String request = """
                {
                  "locality": 0,
                  "frugality": 5,
                  "flexibility": 3,
                  "experientiality": 4,
                  "vitality": 2,
                  "sociality": 1
                }
                """;

            mockMvc.perform(post("/propensity")
                    .header("Authorization", "Bearer " + TokenFixture.ACCESS_TOKEN)
                    .contentType(APPLICATION_JSON)
                    .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("INVALID_INPUT_VALUE"));
        }
    }

    @Nested
    class 조회 {

        @Test
        void 취향_진단_결과를_조회한다() throws Exception {
            인증된_사용자();
            when(propensityService.getPropensity(AuthResponseFixture.USER_ID))
                .thenReturn(PropensityResponseFixture.propensityResponse());

            mockMvc.perform(get("/propensity")
                    .header("Authorization", "Bearer " + TokenFixture.ACCESS_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.result.type").value(PropensityResponseFixture.TYPE))
                .andExpect(jsonPath("$.data.locality").value(PropensityRequestFixture.LOCALITY))
                .andExpect(jsonPath("$.data.sociality").value(PropensityRequestFixture.SOCIALITY));
        }

        @Test
        void 인증_토큰이_없으면_예외를_응답한다() throws Exception {
            mockMvc.perform(get("/propensity"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("INVALID_ACCESS_TOKEN"));
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
