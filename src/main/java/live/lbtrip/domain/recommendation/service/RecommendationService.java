package live.lbtrip.domain.recommendation.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import live.lbtrip.domain.admin.incentive.model.Incentive;
import live.lbtrip.domain.recommendation.dto.response.CourseCandidateResponse;
import live.lbtrip.domain.recommendation.dto.response.CourseDetailResponse;
import live.lbtrip.domain.recommendation.dto.response.RegionRecommendationResponse;
import live.lbtrip.domain.recommendation.model.entity.GeneratedCourse;
import live.lbtrip.domain.recommendation.model.entity.RecommendedRegion;
import live.lbtrip.domain.user.model.User;
import live.lbtrip.domain.user.service.UserFinder;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationService {

    private final RecommendedRegionFinder recommendedRegionFinder;
    private final GeneratedCourseFinder generatedCourseFinder;
    private final IncentiveFinder incentiveFinder;
    private final SaveCourseManager saveCourseManager;
    private final SaveCourseValidator saveCourseValidator;
    private final UserFinder userFinder;

    public List<RegionRecommendationResponse> getRecommendedRegions(Long userId) {
        return recommendedRegionFinder.findAllByUserId(userId).stream()
            .map(RegionRecommendationResponse::from)
            .toList();
    }

    public List<CourseCandidateResponse> getRegionCourses(Long userId, Long regionId) {
        return recommendedRegionFinder.findByIdAndUserId(regionId, userId).getCourses().stream()
            .map(CourseCandidateResponse::from)
            .toList();
    }

    public CourseDetailResponse getCourseDetail(Long userId, Long courseId) {
        GeneratedCourse course = generatedCourseFinder.findByIdAndUserId(courseId, userId);
        RecommendedRegion recommendedRegion = course.getRecommendedRegion();
        List<Incentive> incentives = incentiveFinder.findAllByRegion(
            recommendedRegion.getLdongRegnCd(),
            recommendedRegion.getLdongSignguCd()
        );

        return CourseDetailResponse.of(course, incentives);
    }

    @Transactional
    public void saveCourse(Long userId, Long courseId) {
        saveCourseValidator.validateNew(userId, courseId);
        GeneratedCourse course = generatedCourseFinder.findByIdAndUserId(courseId, userId);
        User user = userFinder.findById(userId);
        saveCourseManager.add(course, user);
    }
}
