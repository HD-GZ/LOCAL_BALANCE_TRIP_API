package live.lbtrip.domain.home.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
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
import live.lbtrip.domain.auth.model.JwtTokenSubject;
import live.lbtrip.domain.auth.service.JwtTokenProvider;
import live.lbtrip.domain.home.dto.response.HeroResponse;
import live.lbtrip.domain.home.dto.response.HomeFeedResponse;
import live.lbtrip.domain.home.dto.response.HomeIncentiveResponse;
import live.lbtrip.domain.home.dto.response.PopularCourseListResponse;
import live.lbtrip.domain.home.dto.response.ProfileSummaryResponse;
import live.lbtrip.domain.home.dto.response.ProfileTypeListResponse;
import live.lbtrip.domain.home.service.HomeService;
import live.lbtrip.global.config.CorsProperties;
import live.lbtrip.support.fixture.AuthResponseFixture;
import live.lbtrip.support.fixture.TokenFixture;

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
    void 비로그인_히어로를_조회한다() throws Exception {
        when(homeService.getHero(null)).thenReturn(
            HeroResponse.of(List.of(new HeroResponse.InnerHeroItem("https://img/a.jpg", "담양"))));

        mockMvc.perform(get("/home/hero"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result").value("SUCCESS"))
            .andExpect(jsonPath("$.data.items[0].title").value("담양"));
    }

    @Test
    void 로그인_히어로를_조회한다() throws Exception {
        인증된_사용자();
        when(homeService.getHero(AuthResponseFixture.USER_ID)).thenReturn(
            HeroResponse.of(List.of(new HeroResponse.InnerHeroItem("https://img/r.jpg", "전라남도 담양군"))));

        mockMvc.perform(get("/home/hero")
                .header("Authorization", "Bearer " + TokenFixture.ACCESS_TOKEN))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.items[0].title").value("전라남도 담양군"));
    }

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

    @Test
    void 진단_요약을_조회한다() throws Exception {
        인증된_사용자();
        when(homeService.getProfileSummary(AuthResponseFixture.USER_ID)).thenReturn(
            new ProfileSummaryResponse("찐로컬 탐험가 (LVEAI)", "설명", "https://img/lveai.png",
                LocalDate.of(2026, 7, 20),
                List.of(new ProfileSummaryResponse.InnerSlider("LOCALITY", "핫플·유명 명소", "로컬·골목 상권", 4))));

        mockMvc.perform(get("/home/profile-summary")
                .header("Authorization", "Bearer " + TokenFixture.ACCESS_TOKEN))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result").value("SUCCESS"))
            .andExpect(jsonPath("$.data.type").value("찐로컬 탐험가 (LVEAI)"))
            .andExpect(jsonPath("$.data.sliders[0].score").value(4));
    }

    @Test
    void 진단_요약은_토큰이_없으면_예외를_응답한다() throws Exception {
        mockMvc.perform(get("/home/profile-summary"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.result").value("ERROR"))
            .andExpect(jsonPath("$.error.code").value("INVALID_ACCESS_TOKEN"));
    }

    @Test
    void 인기_코스_목록을_조회한다() throws Exception {
        when(homeService.getPopularCourses()).thenReturn(new PopularCourseListResponse(List.of(
            new PopularCourseListResponse.InnerPopularCourse(10L, "담양 골목 미식 코스", "로컬 미식", "https://img/c.jpg", "전라남도 담양군"))));

        mockMvc.perform(get("/home/popular-courses"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.courses[0].courseId").value(10))
            .andExpect(jsonPath("$.data.courses[0].regionName").value("전라남도 담양군"));
    }

    @Test
    void 저장_코스_피드를_조회한다() throws Exception {
        인증된_사용자();
        when(homeService.getSavedCourseFeed(AuthResponseFixture.USER_ID)).thenReturn(
            HomeFeedResponse.of(List.of(
                new HomeFeedResponse.InnerFeedItem("SAVED_COURSE", 1L, "코스A", "https://img/a.jpg", "COMPLETED"),
                new HomeFeedResponse.InnerFeedItem("RECOMMENDED_REGION", 7L, "전라남도 담양군", "https://img/r.jpg", "추천 이유"))));

        mockMvc.perform(get("/home/saved-courses")
                .header("Authorization", "Bearer " + TokenFixture.ACCESS_TOKEN))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.items[0].itemType").value("SAVED_COURSE"))
            .andExpect(jsonPath("$.data.items[1].itemType").value("RECOMMENDED_REGION"));
    }

    @Test
    void 저장_코스_피드는_토큰이_없으면_예외를_응답한다() throws Exception {
        mockMvc.perform(get("/home/saved-courses"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error.code").value("INVALID_ACCESS_TOKEN"));
    }

    @Test
    void 비로그인_진행중_인센티브를_조회한다() throws Exception {
        when(homeService.getIncentives(null)).thenReturn(HomeIncentiveResponse.of(List.of(
            new HomeIncentiveResponse.InnerRegionTab("전라남도 담양군", "46", "710", List.of(
                new HomeIncentiveResponse.InnerIncentive(
                    "담양 로컬 여행 지원", "설명", "https://event.example.com/damyang",
                    LocalDate.now().plusDays(12), 12L))))));

        mockMvc.perform(get("/home/incentives"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result").value("SUCCESS"))
            .andExpect(jsonPath("$.data.regions[0].regionName").value("전라남도 담양군"))
            .andExpect(jsonPath("$.data.regions[0].incentives[0].dday").value(12));
    }

    @Test
    void 로그인_진행중_인센티브를_조회한다() throws Exception {
        인증된_사용자();
        when(homeService.getIncentives(AuthResponseFixture.USER_ID)).thenReturn(HomeIncentiveResponse.of(List.of(
            new HomeIncentiveResponse.InnerRegionTab("전라남도 담양군", "46", "710", List.of(
                new HomeIncentiveResponse.InnerIncentive(
                    "담양 로컬 여행 지원", "설명", "https://event.example.com/damyang",
                    LocalDate.now().plusDays(12), 12L))))));

        mockMvc.perform(get("/home/incentives")
                .header("Authorization", "Bearer " + TokenFixture.ACCESS_TOKEN))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.regions[0].regionName").value("전라남도 담양군"));
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
