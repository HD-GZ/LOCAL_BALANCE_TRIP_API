package live.lbtrip.domain.home.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

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
import live.lbtrip.domain.home.dto.response.ProfileTypeListResponse;
import live.lbtrip.domain.home.service.HomeService;
import live.lbtrip.global.config.CorsProperties;

@WebMvcTest(HomeController.class)
@Import(HomeControllerTest.TestCorsConfig.class)
class HomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private HomeService homeService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMappingContext;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private AdminJwtTokenProvider adminJwtTokenProvider;

    @Test
    void 대표_유형_목록을_조회한다() throws Exception {
        when(homeService.getProfileTypes()).thenReturn(new ProfileTypeListResponse(List.of(
            new ProfileTypeListResponse.InnerProfileType("LVEAI", "찐로컬 탐험가", "설명", "https://img/lveai.png"))));

        mockMvc.perform(get("/home/profile-types"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result").value("SUCCESS"))
            .andExpect(jsonPath("$.data.types[0].code").value("LVEAI"))
            .andExpect(jsonPath("$.data.types[0].nickname").value("찐로컬 탐험가"));
    }

    @TestConfiguration
    static class TestCorsConfig {

        @Bean
        CorsProperties corsProperties() {
            return new CorsProperties(List.of("http://localhost"));
        }
    }
}
