package live.lbtrip.domain.home.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import live.lbtrip.domain.home.dto.response.HeroResponse;
import live.lbtrip.domain.home.dto.response.HomeFeedResponse;
import live.lbtrip.domain.home.dto.response.HomeIncentiveResponse;
import live.lbtrip.domain.home.dto.response.PopularCourseListResponse;
import live.lbtrip.domain.home.dto.response.ProfileSummaryResponse;
import live.lbtrip.domain.home.dto.response.ProfileTypeListResponse;
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
import live.lbtrip.domain.recommendation.service.RecommendationService;
import live.lbtrip.domain.savedcourse.course.dto.response.SavedCourseListResponse;
import live.lbtrip.domain.savedcourse.course.service.SavedCourseService;
import live.lbtrip.domain.tourism.repository.TourPlaceRepository;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import live.lbtrip.global.storage.service.ImageStorage;
import live.lbtrip.global.web.PageQueryRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HomeService {

    public static final int HERO_SIZE = 5;
    public static final int POPULAR_REGION_SIZE = 6;
    private static final int SAVED_FEED_PAGE_SIZE = 20;
    private static final int SAVED_PER_RECOMMENDATION = 2;

    private final TravelProfileRepository travelProfileRepository;
    private final ImageStorage imageStorage;
    private final PropensityFinder propensityFinder;
    private final TravelProfileFinder travelProfileFinder;
    private final PropensityFactorSelector propensityFactorSelector;
    private final TourPlaceRepository tourPlaceRepository;
    private final RecommendedRegionRepository recommendedRegionRepository;
    private final GeneratedCourseRepository generatedCourseRepository;
    private final SavedCourseService savedCourseService;
    private final RecommendationService recommendationService;
    private final IncentiveFinder incentiveFinder;

    public HeroResponse getHero(Long userId) {
        List<HeroResponse.InnerHeroItem> items = (userId == null)
            ? tourPlaceRepository.findRandomWithImage(HERO_SIZE).stream()
                .map(p -> new HeroResponse.InnerHeroItem(p.getImageUrl(), p.getTitle()))
                .toList()
            : recommendedRegionRepository.findAllByUserIdOrderByDisplayOrder(userId).stream()
                .filter(r -> r.getImageUrl() != null)
                .map(r -> new HeroResponse.InnerHeroItem(r.getImageUrl(), r.getRegionName()))
                .toList();
        return HeroResponse.of(items);
    }

    public ProfileTypeListResponse getProfileTypes() {
        List<ProfileTypeListResponse.InnerProfileType> types = travelProfileRepository
            .findByFeaturedOrderIsNotNullOrderByFeaturedOrderAsc().stream()
            .map(p -> new ProfileTypeListResponse.InnerProfileType(
                p.getCode(),
                p.getNickname(),
                p.getDescription(),
                imageStorage.publicUrl(p.getImageKey())))
            .toList();
        return ProfileTypeListResponse.of(types);
    }

    public ProfileSummaryResponse getProfileSummary(Long userId) {
        Propensity propensity = propensityFinder.findByUserId(userId);
        Preference preference = propensity.getPreference();
        ValueConsumption valueConsumption = propensity.getValueConsumption();
        TravelProfile profile = travelProfileFinder.findByPreference(preference);

        return ProfileSummaryResponse.of(
            profile,
            imageStorage.publicUrl(profile.getImageKey()),
            propensity.getUpdatedAt().toLocalDate(),
            preference,
            valueConsumption,
            propensityFactorSelector.selectThree());
    }

    public PopularCourseListResponse getPopularCourses() {
        List<GeneratedCourse> courses = recommendedRegionRepository
            .findPopularRegions(PageRequest.of(0, POPULAR_REGION_SIZE)).stream()
            .map(popular -> generatedCourseRepository
                .findFirstByRecommendedRegionRegionCandidateIdOrderByIdAsc(popular.getRegionCandidateId()))
            .flatMap(Optional::stream)
            .toList();
        return PopularCourseListResponse.of(courses);
    }

    public CourseDetailResponse getPopularCourseDetail(Long courseId) {
        GeneratedCourse course = generatedCourseRepository.findById(courseId)
            .orElseThrow(() -> BusinessException.of(ErrorCode.COURSE_NOT_FOUND));
        RecommendedRegion region = course.getRecommendedRegion();
        List<Incentive> incentives = incentiveFinder.findActiveByRegion(
            region.getRegionCandidate().getId(), LocalDate.now());
        return CourseDetailResponse.of(course, incentives);
    }

    public HomeFeedResponse getSavedCourseFeed(Long userId) {
        List<SavedCourseListResponse.InnerSavedCourseResponse> saved =
            savedCourseService.getSavedCourses(userId, null, new PageQueryRequest(1, SAVED_FEED_PAGE_SIZE)).courses();
        List<RegionRecommendationResponse> regions = recommendationService.getRecommendedRegions(userId);

        List<HomeFeedResponse.InnerFeedItem> items = new ArrayList<>();
        int regionIndex = 0;
        for (int i = 0; i < saved.size(); i++) {
            SavedCourseListResponse.InnerSavedCourseResponse course = saved.get(i);
            items.add(new HomeFeedResponse.InnerFeedItem(
                "SAVED_COURSE", course.savedCourseId(), course.courseName(),
                course.imageUrl(), course.status().name()));
            boolean boundary = (i + 1) % SAVED_PER_RECOMMENDATION == 0;
            if (boundary && regionIndex < regions.size()) {
                RegionRecommendationResponse region = regions.get(regionIndex++);
                items.add(new HomeFeedResponse.InnerFeedItem(
                    "RECOMMENDED_REGION", region.regionId(), region.regionName(),
                    region.imageUrl(), region.reason()));
            }
        }
        while (regionIndex < regions.size()) {
            RegionRecommendationResponse region = regions.get(regionIndex++);
            items.add(new HomeFeedResponse.InnerFeedItem(
                "RECOMMENDED_REGION", region.regionId(), region.regionName(),
                region.imageUrl(), region.reason()));
        }
        return HomeFeedResponse.of(items);
    }

    public HomeIncentiveResponse getIncentives(Long userId) {
        LocalDate today = LocalDate.now();
        List<HomeIncentiveResponse.InnerRegionTab> tabs = (userId == null)
            ? popularRegionTabs(today)
            : myRegionTabs(userId, today);
        return HomeIncentiveResponse.of(tabs);
    }

    private List<HomeIncentiveResponse.InnerRegionTab> myRegionTabs(Long userId, LocalDate today) {
        return recommendedRegionRepository.findAllByUserIdOrderByDisplayOrder(userId).stream()
            .map(r -> HomeIncentiveResponse.tab(
                r.getRegionName(), r.getRegionCandidate().getId(),
                incentiveFinder.findActiveByRegion(r.getRegionCandidate().getId(), today),
                today))
            .filter(tab -> !tab.incentives().isEmpty())
            .toList();
    }

    private List<HomeIncentiveResponse.InnerRegionTab> popularRegionTabs(LocalDate today) {
        return recommendedRegionRepository.findPopularRegions(PageRequest.of(0, POPULAR_REGION_SIZE)).stream()
            .map(popular -> recommendedRegionRepository
                .findFirstByRegionCandidateId(popular.getRegionCandidateId())
                .map(region -> HomeIncentiveResponse.tab(
                    region.getRegionName(),
                    region.getRegionCandidate().getId(),
                    incentiveFinder.findActiveByRegion(region.getRegionCandidate().getId(), today),
                    today))
                .orElse(null))
            .filter(Objects::nonNull)
            .filter(tab -> !tab.incentives().isEmpty())
            .toList();
    }
}
