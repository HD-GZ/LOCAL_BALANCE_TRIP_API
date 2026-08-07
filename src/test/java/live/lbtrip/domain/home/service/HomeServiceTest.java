package live.lbtrip.domain.home.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import live.lbtrip.domain.home.dto.response.HeroResponse;
import live.lbtrip.domain.home.dto.response.HomeFeedResponse;
import live.lbtrip.domain.home.dto.response.PopularCourseListResponse;
import live.lbtrip.domain.home.dto.response.ProfileSummaryResponse;
import live.lbtrip.domain.home.dto.response.ProfileTypeListResponse;
import live.lbtrip.domain.home.model.PropensityFactor;
import live.lbtrip.domain.propensity.model.Preference;
import live.lbtrip.domain.propensity.model.Propensity;
import live.lbtrip.domain.propensity.model.TravelProfile;
import live.lbtrip.domain.propensity.model.ValueConsumption;
import live.lbtrip.domain.propensity.repository.TravelProfileRepository;
import live.lbtrip.domain.propensity.service.PropensityFinder;
import live.lbtrip.domain.propensity.service.TravelProfileFinder;
import live.lbtrip.domain.recommendation.dto.response.RegionRecommendationResponse;
import live.lbtrip.domain.recommendation.model.entity.GeneratedCourse;
import live.lbtrip.domain.recommendation.model.entity.RecommendedRegion;
import live.lbtrip.domain.recommendation.repository.GeneratedCourseRepository;
import live.lbtrip.domain.recommendation.repository.RecommendedRegionRepository;
import live.lbtrip.domain.recommendation.repository.dto.PopularRegionCode;
import live.lbtrip.domain.recommendation.service.RecommendationService;
import live.lbtrip.domain.savedcourse.course.dto.response.SavedCourseListResponse;
import live.lbtrip.domain.savedcourse.course.service.SavedCourseService;
import live.lbtrip.domain.savedcourse.model.enums.SavedCourseStatus;
import live.lbtrip.domain.tourism.model.entity.TourPlace;
import live.lbtrip.domain.tourism.repository.TourPlaceRepository;
import live.lbtrip.global.storage.service.ImageStorage;
import live.lbtrip.support.fixture.TravelProfileFixture;

@ExtendWith(MockitoExtension.class)
class HomeServiceTest {

    @Mock private TravelProfileRepository travelProfileRepository;
    @Mock private ImageStorage imageStorage;
    @Mock private PropensityFinder propensityFinder;
    @Mock private TravelProfileFinder travelProfileFinder;
    @Mock private PropensityFactorSelector propensityFactorSelector;
    @Mock private TourPlaceRepository tourPlaceRepository;
    @Mock private RecommendedRegionRepository recommendedRegionRepository;
    @Mock private GeneratedCourseRepository generatedCourseRepository;
    @Mock private SavedCourseService savedCourseService;
    @Mock private RecommendationService recommendationService;
    @InjectMocks private HomeService homeService;

    @Test
    void 대표_유형을_featured_order_순으로_반환한다() {
        List<TravelProfile> profiles = List.of(
            TravelProfileFixture.featured("LVEAI", "찐로컬 탐험가", 1),
            TravelProfileFixture.featured("HVEAG", "미식 수집가", 2));
        when(travelProfileRepository.findByFeaturedOrderIsNotNullOrderByFeaturedOrderAsc())
            .thenReturn(profiles);
        when(imageStorage.publicUrl("travel-profiles/lveai.png")).thenReturn("https://img/lveai.png");
        when(imageStorage.publicUrl("travel-profiles/hveag.png")).thenReturn("https://img/hveag.png");

        ProfileTypeListResponse response = homeService.getProfileTypes();

        assertThat(response.types()).hasSize(2);
        assertThat(response.types().get(0).nickname()).isEqualTo("찐로컬 탐험가");
        assertThat(response.types().get(0).imageUrl()).isEqualTo("https://img/lveai.png");
    }

    @Test
    void 진단_요약을_슬라이더_세_개와_함께_반환한다() {
        Long userId = 1L;
        Propensity propensity = mock(Propensity.class);
        Preference preference = Preference.of(4, 5, 4, 2, 4);
        ValueConsumption vc = ValueConsumption.of(2, 4, 5, 2, 4);
        TravelProfile profile = TravelProfileFixture.featured("LVEAI", "찐로컬 탐험가", 1);

        when(propensityFinder.findByUserId(userId)).thenReturn(propensity);
        when(propensity.getPreference()).thenReturn(preference);
        when(propensity.getValueConsumption()).thenReturn(vc);
        when(propensity.getUpdatedAt()).thenReturn(LocalDateTime.of(2026, 7, 20, 9, 0));
        when(travelProfileFinder.findByPreference(preference)).thenReturn(profile);
        when(imageStorage.publicUrl("travel-profiles/lveai.png")).thenReturn("https://img/lveai.png");
        when(propensityFactorSelector.selectThree()).thenReturn(List.of(
            PropensityFactor.LOCALITY, PropensityFactor.VITALITY, PropensityFactor.SOCIALITY));

        ProfileSummaryResponse response = homeService.getProfileSummary(userId);

        assertThat(response.type()).isEqualTo("찐로컬 탐험가 (LVEAI)");
        assertThat(response.diagnosedAt()).isEqualTo(LocalDate.of(2026, 7, 20));
        assertThat(response.sliders()).hasSize(3);
        assertThat(response.sliders().get(0).minLabel()).isEqualTo("핫플·유명 명소");
        assertThat(response.sliders().get(0).score()).isEqualTo(4);
    }

    @Test
    void 비로그인_히어로는_랜덤_투어플레이스_사진을_반환한다() {
        TourPlace place = live.lbtrip.support.fixture.TourPlaceFixture.withImage("담양 메타세쿼이아길", "https://img/damyang.jpg");
        when(tourPlaceRepository.findRandomWithImage(HomeService.HERO_SIZE)).thenReturn(List.of(place));

        HeroResponse response = homeService.getHero(null);

        assertThat(response.items()).hasSize(1);
        assertThat(response.items().get(0).imageUrl()).isEqualTo("https://img/damyang.jpg");
        assertThat(response.items().get(0).title()).isEqualTo("담양 메타세쿼이아길");
    }

    @Test
    void 로그인_히어로는_내_추천지역_사진을_반환한다() {
        RecommendedRegion region = org.mockito.Mockito.mock(RecommendedRegion.class);
        when(region.getRegionName()).thenReturn("전라남도 담양군");
        when(region.getImageUrl()).thenReturn("https://img/region.jpg");
        when(recommendedRegionRepository.findAllByUserIdOrderByDisplayOrder(1L)).thenReturn(List.of(region));

        HeroResponse response = homeService.getHero(1L);

        assertThat(response.items()).hasSize(1);
        assertThat(response.items().get(0).title()).isEqualTo("전라남도 담양군");
        assertThat(response.items().get(0).imageUrl()).isEqualTo("https://img/region.jpg");
    }

    @Test
    void 인기_지역의_대표_코스를_반환한다() {
        PopularRegionCode code = new PopularRegionCode() {
            @Override public String getLdongRegnCd() { return "46"; }
            @Override public String getLdongSignguCd() { return "710"; }
        };
        when(recommendedRegionRepository.findPopularRegionCodes(
            org.springframework.data.domain.PageRequest.of(0, HomeService.POPULAR_REGION_SIZE)))
            .thenReturn(List.of(code));

        GeneratedCourse course = org.mockito.Mockito.mock(GeneratedCourse.class);
        RecommendedRegion region = org.mockito.Mockito.mock(RecommendedRegion.class);
        when(course.getId()).thenReturn(10L);
        when(course.getName()).thenReturn("담양 골목 미식 코스");
        when(course.getReason()).thenReturn("로컬 미식 동선");
        when(course.getImageUrl()).thenReturn("https://img/course.jpg");
        when(course.getRecommendedRegion()).thenReturn(region);
        when(region.getRegionName()).thenReturn("전라남도 담양군");
        when(generatedCourseRepository
            .findFirstByRecommendedRegion_LdongRegnCdAndRecommendedRegion_LdongSignguCdOrderByIdAsc("46", "710"))
            .thenReturn(java.util.Optional.of(course));

        PopularCourseListResponse response = homeService.getPopularCourses();

        assertThat(response.courses()).hasSize(1);
        assertThat(response.courses().get(0).courseId()).isEqualTo(10L);
        assertThat(response.courses().get(0).regionName()).isEqualTo("전라남도 담양군");
    }

    @Test
    void 저장_코스_두개마다_추천여행지를_끼워_넣는다() {
        Long userId = 1L;
        SavedCourseListResponse saved = new SavedCourseListResponse(3, 1, 10, 1, List.of(
            new SavedCourseListResponse.InnerSavedCourseResponse(1L, "코스A", "https://img/a.jpg", SavedCourseStatus.COMPLETED),
            new SavedCourseListResponse.InnerSavedCourseResponse(2L, "코스B", "https://img/b.jpg", SavedCourseStatus.BEFORE_TRIP),
            new SavedCourseListResponse.InnerSavedCourseResponse(3L, "코스C", "https://img/c.jpg", SavedCourseStatus.BEFORE_TRIP)));
        when(savedCourseService.getSavedCourses(eq(userId), isNull(), any())).thenReturn(saved);
        when(recommendationService.getRecommendedRegions(userId)).thenReturn(List.of(
            new RegionRecommendationResponse(7L, "전라남도 담양군", "https://img/r.jpg", "추천 이유")));

        HomeFeedResponse response = homeService.getSavedCourseFeed(userId);

        // 코스A, 코스B, 추천(담양), 코스C
        assertThat(response.items()).hasSize(4);
        assertThat(response.items().get(0).itemType()).isEqualTo("SAVED_COURSE");
        assertThat(response.items().get(2).itemType()).isEqualTo("RECOMMENDED_REGION");
        assertThat(response.items().get(2).title()).isEqualTo("전라남도 담양군");
        assertThat(response.items().get(3).itemType()).isEqualTo("SAVED_COURSE");
    }
}
