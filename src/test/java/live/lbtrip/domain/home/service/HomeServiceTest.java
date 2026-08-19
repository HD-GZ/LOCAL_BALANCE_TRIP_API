package live.lbtrip.domain.home.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import live.lbtrip.domain.home.dto.response.HeroResponse;
import live.lbtrip.domain.home.dto.response.HomeFeedResponse;
import live.lbtrip.domain.home.dto.response.HomeIncentiveResponse;
import live.lbtrip.domain.home.dto.response.PopularCourseListResponse;
import live.lbtrip.domain.home.dto.response.ProfileSummaryResponse;
import live.lbtrip.domain.home.dto.response.ProfileTypeListResponse;
import live.lbtrip.domain.home.model.PropensityFactor;
import live.lbtrip.domain.incentive.model.Incentive;
import live.lbtrip.domain.incentive.service.IncentiveFinder;
import live.lbtrip.domain.propensity.model.Preference;
import live.lbtrip.domain.propensity.model.Propensity;
import live.lbtrip.domain.propensity.model.TravelProfile;
import live.lbtrip.domain.propensity.model.ValueConsumption;
import live.lbtrip.domain.propensity.repository.TravelProfileRepository;
import live.lbtrip.domain.propensity.service.PropensityFinder;
import live.lbtrip.domain.propensity.service.TravelProfileFinder;
import live.lbtrip.domain.recommendation.dto.response.CourseDetailResponse;
import live.lbtrip.domain.recommendation.dto.response.RegionRecommendationResponse;
import live.lbtrip.domain.recommendation.model.entity.GeneratedCourse;
import live.lbtrip.domain.recommendation.model.entity.RecommendedRegion;
import live.lbtrip.domain.recommendation.repository.GeneratedCourseRepository;
import live.lbtrip.domain.recommendation.repository.RecommendedRegionRepository;
import live.lbtrip.domain.recommendation.repository.dto.PopularRegion;
import live.lbtrip.domain.recommendation.service.RecommendationService;
import live.lbtrip.domain.region.model.RegionCandidate;
import live.lbtrip.domain.savedcourse.course.dto.response.SavedCourseListResponse;
import live.lbtrip.domain.savedcourse.course.service.SavedCourseService;
import live.lbtrip.domain.savedcourse.model.enums.SavedCourseStatus;
import live.lbtrip.domain.tourism.model.entity.TourPlace;
import live.lbtrip.domain.tourism.repository.TourPlaceRepository;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import live.lbtrip.global.i18n.MessageResolver;
import live.lbtrip.global.storage.service.ImageStorage;
import live.lbtrip.support.fixture.RegionCandidateFixture;
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
    @Mock private IncentiveFinder incentiveFinder;
    @Mock private MessageResolver messageResolver;
    @InjectMocks private HomeService homeService;

    @BeforeEach
    void setUpLocale() {
        lenient().when(messageResolver.currentLocale()).thenReturn(Locale.KOREAN);
    }

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
        when(tourPlaceRepository.findRandomWithImage("ko", HomeService.HERO_SIZE)).thenReturn(List.of(place));

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
        when(recommendedRegionRepository.findAllByUserIdAndLocaleOrderByDisplayOrder(1L, Locale.KOREAN)).thenReturn(List.of(region));

        HeroResponse response = homeService.getHero(1L);

        assertThat(response.items()).hasSize(1);
        assertThat(response.items().get(0).title()).isEqualTo("전라남도 담양군");
        assertThat(response.items().get(0).imageUrl()).isEqualTo("https://img/region.jpg");
    }

    @Test
    void 로그인_히어로는_이미지가_없는_추천지역을_제외한다() {
        RecommendedRegion withImage = org.mockito.Mockito.mock(RecommendedRegion.class);
        when(withImage.getRegionName()).thenReturn("전라남도 담양군");
        when(withImage.getImageUrl()).thenReturn("https://img/region.jpg");
        RecommendedRegion withoutImage = org.mockito.Mockito.mock(RecommendedRegion.class);
        when(withoutImage.getImageUrl()).thenReturn(null);
        when(recommendedRegionRepository.findAllByUserIdAndLocaleOrderByDisplayOrder(1L, Locale.KOREAN))
            .thenReturn(List.of(withImage, withoutImage));

        HeroResponse response = homeService.getHero(1L);

        assertThat(response.items()).hasSize(1);
        assertThat(response.items().get(0).title()).isEqualTo("전라남도 담양군");
        assertThat(response.items().get(0).imageUrl()).isEqualTo("https://img/region.jpg");
    }

    @Test
    void 인기_지역의_대표_코스를_반환한다() {
        PopularRegion popular = () -> RegionCandidateFixture.CANDIDATE_ID;
        when(recommendedRegionRepository.findPopularRegions(
            Locale.KOREAN, org.springframework.data.domain.PageRequest.of(0, HomeService.POPULAR_REGION_SIZE)))
            .thenReturn(List.of(popular));

        GeneratedCourse course = org.mockito.Mockito.mock(GeneratedCourse.class);
        RecommendedRegion region = org.mockito.Mockito.mock(RecommendedRegion.class);
        when(course.getId()).thenReturn(10L);
        when(course.getName()).thenReturn("담양 골목 미식 코스");
        when(course.getReason()).thenReturn("로컬 미식 동선");
        when(course.getImageUrl()).thenReturn("https://img/course.jpg");
        when(course.getRecommendedRegion()).thenReturn(region);
        when(region.getRegionName()).thenReturn("전라남도 담양군");
        when(generatedCourseRepository
            .findFirstByRecommendedRegionRegionCandidateIdAndRecommendedRegionLocaleOrderByIdAsc(
                RegionCandidateFixture.CANDIDATE_ID, Locale.KOREAN))
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

    @Test
    void 로그인_진행중_인센티브는_내_추천지역_탭별로_dday와_함께_반환한다() {
        Long userId = 1L;
        RecommendedRegion region = org.mockito.Mockito.mock(RecommendedRegion.class);
        when(region.getRegionName()).thenReturn("전라남도 담양군");
        when(region.getRegionCandidate()).thenReturn(RegionCandidateFixture.candidateWithId());
        when(recommendedRegionRepository.findAllByUserIdAndLocaleOrderByDisplayOrder(userId, Locale.KOREAN)).thenReturn(List.of(region));

        Incentive incentive = Incentive.create(
            "담양 로컬 여행 지원", "https://event.example.com/damyang", "설명",
            LocalDate.now().minusDays(1), LocalDate.now().plusDays(12));
        when(incentiveFinder.findActiveByRegion(eq(RegionCandidateFixture.CANDIDATE_ID), any()))
            .thenReturn(List.of(incentive));

        HomeIncentiveResponse response = homeService.getIncentives(userId);

        assertThat(response.regions()).hasSize(1);
        assertThat(response.regions().get(0).regionName()).isEqualTo("전라남도 담양군");
        assertThat(response.regions().get(0).regionCandidateId()).isEqualTo(RegionCandidateFixture.CANDIDATE_ID);
        assertThat(response.regions().get(0).incentives().get(0).dday()).isEqualTo(12L);
    }

    @Test
    void 진행중_인센티브가_없는_추천지역_탭은_제외한다() {
        Long userId = 1L;
        RecommendedRegion withIncentive = org.mockito.Mockito.mock(RecommendedRegion.class);
        when(withIncentive.getRegionName()).thenReturn("전라남도 담양군");
        when(withIncentive.getRegionCandidate()).thenReturn(RegionCandidateFixture.candidateWithId());
        RecommendedRegion withoutIncentive = org.mockito.Mockito.mock(RecommendedRegion.class);
        RegionCandidate withoutIncentiveCandidate = RegionCandidate.create("충청남도 홍성군", "44", "150");
        org.springframework.test.util.ReflectionTestUtils.setField(withoutIncentiveCandidate, "id", 2L);
        when(withoutIncentive.getRegionCandidate())
            .thenReturn(withoutIncentiveCandidate);
        when(recommendedRegionRepository.findAllByUserIdAndLocaleOrderByDisplayOrder(userId, Locale.KOREAN))
            .thenReturn(List.of(withIncentive, withoutIncentive));

        Incentive incentive = Incentive.create(
            "담양 로컬 여행 지원", "https://event.example.com/damyang", "설명",
            LocalDate.now().minusDays(1), LocalDate.now().plusDays(12));
        when(incentiveFinder.findActiveByRegion(eq(RegionCandidateFixture.CANDIDATE_ID), any()))
            .thenReturn(List.of(incentive));
        when(incentiveFinder.findActiveByRegion(eq(2L), any())).thenReturn(List.of());

        HomeIncentiveResponse response = homeService.getIncentives(userId);

        assertThat(response.regions()).hasSize(1);
        assertThat(response.regions().get(0).regionName()).isEqualTo("전라남도 담양군");
    }

    @Test
    void 비로그인_진행중_인센티브는_인기_지역_탭별로_반환한다() {
        PopularRegion popular = () -> RegionCandidateFixture.CANDIDATE_ID;
        when(recommendedRegionRepository.findPopularRegions(
            Locale.KOREAN, org.springframework.data.domain.PageRequest.of(0, HomeService.POPULAR_REGION_SIZE)))
            .thenReturn(List.of(popular));

        RecommendedRegion region = org.mockito.Mockito.mock(RecommendedRegion.class);
        when(region.getRegionName()).thenReturn("전라남도 담양군");
        when(region.getRegionCandidate()).thenReturn(RegionCandidateFixture.candidateWithId());
        when(recommendedRegionRepository.findFirstByRegionCandidateIdAndLocale(RegionCandidateFixture.CANDIDATE_ID, Locale.KOREAN))
            .thenReturn(java.util.Optional.of(region));

        Incentive incentive = Incentive.create(
            "담양 로컬 여행 지원", "https://event.example.com/damyang", "설명",
            LocalDate.now().minusDays(1), LocalDate.now().plusDays(12));
        when(incentiveFinder.findActiveByRegion(eq(RegionCandidateFixture.CANDIDATE_ID), any()))
            .thenReturn(List.of(incentive));

        HomeIncentiveResponse response = homeService.getIncentives(null);

        assertThat(response.regions()).hasSize(1);
        assertThat(response.regions().get(0).regionName()).isEqualTo("전라남도 담양군");
        assertThat(response.regions().get(0).regionCandidateId()).isEqualTo(RegionCandidateFixture.CANDIDATE_ID);
        assertThat(response.regions().get(0).incentives()).hasSize(1);
    }

    @Test
    void 공개_인기_코스_상세를_조회한다() {
        GeneratedCourse course = org.mockito.Mockito.mock(GeneratedCourse.class);
        RecommendedRegion region = org.mockito.Mockito.mock(RecommendedRegion.class);
        when(course.getId()).thenReturn(10L);
        when(course.getName()).thenReturn("담양 골목 미식 코스");
        when(course.getPlaces()).thenReturn(List.of());
        when(course.getRecommendedRegion()).thenReturn(region);
        when(region.getRegionName()).thenReturn("전라남도 담양군");
        when(region.getRegionCandidate()).thenReturn(RegionCandidateFixture.candidateWithId());
        when(generatedCourseRepository.findById(10L)).thenReturn(Optional.of(course));
        when(incentiveFinder.findActiveByRegion(eq(RegionCandidateFixture.CANDIDATE_ID), any(LocalDate.class)))
            .thenReturn(List.of());

        CourseDetailResponse response = homeService.getPopularCourseDetail(10L);

        assertThat(response.courseId()).isEqualTo(10L);
        assertThat(response.title()).isEqualTo("담양 골목 미식 코스");
        assertThat(response.regionName()).isEqualTo("전라남도 담양군");
        assertThat(response.benefits()).isEmpty();
    }

    @Test
    void 공개_인기_코스_상세는_없는_코스면_예외를_던진다() {
        when(generatedCourseRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> homeService.getPopularCourseDetail(999L))
            .isInstanceOf(BusinessException.class)
            .extracting(ex -> ((BusinessException) ex).getErrorCode())
            .isEqualTo(ErrorCode.COURSE_NOT_FOUND);
    }
}
