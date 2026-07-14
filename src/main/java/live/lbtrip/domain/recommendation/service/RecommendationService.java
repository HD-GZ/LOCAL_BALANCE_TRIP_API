package live.lbtrip.domain.recommendation.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import live.lbtrip.domain.recommendation.dto.response.CourseCandidateResponse;
import live.lbtrip.domain.recommendation.dto.response.CourseDetailResponse;
import live.lbtrip.domain.recommendation.dto.response.RegionRecommendationResponse;
import live.lbtrip.domain.recommendation.model.entity.CoursePlace;
import live.lbtrip.domain.recommendation.model.entity.GeneratedCourse;
import live.lbtrip.domain.recommendation.model.entity.RecommendedRegion;
import live.lbtrip.domain.recommendation.model.entity.SavedCourse;
import live.lbtrip.domain.recommendation.model.entity.SavedCoursePlace;
import live.lbtrip.domain.recommendation.repository.GeneratedCourseRepository;
import live.lbtrip.domain.recommendation.repository.RecommendedRegionRepository;
import live.lbtrip.domain.recommendation.repository.SavedCourseRepository;
import live.lbtrip.domain.user.model.User;
import live.lbtrip.domain.user.repository.UserRepository;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationService {

    private final RecommendedRegionRepository recommendedRegionRepository;
    private final GeneratedCourseRepository generatedCourseRepository;
    private final SavedCourseRepository savedCourseRepository;
    private final UserRepository userRepository;

    public List<RegionRecommendationResponse> getRecommendedRegions(Long userId) {
        return recommendedRegionRepository.findAllByUserIdOrderByDisplayOrder(userId).stream()
            .map(RegionRecommendationResponse::from)
            .toList();
    }

    public List<CourseCandidateResponse> getRegionCourses(Long userId, Long regionId) {
        RecommendedRegion region = recommendedRegionRepository.findByIdAndUserId(regionId, userId)
            .orElseThrow(() -> BusinessException.of(ErrorCode.REGION_NOT_FOUND));

        return region.getCourses().stream()
            .map(CourseCandidateResponse::from)
            .toList();
    }

    public CourseDetailResponse getCourseDetail(Long userId, Long courseId) {
        GeneratedCourse course = generatedCourseRepository.findByIdAndUserId(courseId, userId)
            .orElseThrow(() -> BusinessException.of(ErrorCode.COURSE_NOT_FOUND));

        return CourseDetailResponse.of(course);
    }

    @Transactional
    public void saveCourse(Long userId, Long courseId) {
        if (savedCourseRepository.existsByUserIdAndSourceCourseId(userId, courseId)) {
            return;
        }
        GeneratedCourse course = generatedCourseRepository.findByIdAndUserId(courseId, userId)
            .orElseThrow(() -> BusinessException.of(ErrorCode.COURSE_NOT_FOUND));

        User user = userRepository.findById(userId)
            .orElseThrow(() -> BusinessException.of(ErrorCode.USER_NOT_FOUND));
        SavedCourse savedCourse = SavedCourse.create(
            user, course.getId(), course.getName(),
            course.getRecommendedRegion().getRegionName(), course.getReason());

        for (CoursePlace place : course.getPlaces()) {
            savedCourse.addPlace(SavedCoursePlace.create(
                place.getVisitOrder(), place.getName(), place.getOverview(), place.getImageUrl(),
                place.getLatitude(), place.getLongitude(), place.getWalkMinutes(),
                place.isHasAudio(), place.getAudioUrl()));
        }
        savedCourseRepository.save(savedCourse);
    }
}
