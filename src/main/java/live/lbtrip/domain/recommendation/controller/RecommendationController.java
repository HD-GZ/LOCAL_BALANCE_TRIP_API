package live.lbtrip.domain.recommendation.controller;

import static org.springframework.http.HttpStatus.CREATED;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import live.lbtrip.domain.recommendation.dto.response.CourseCandidateResponse;
import live.lbtrip.domain.recommendation.dto.response.CourseDetailResponse;
import live.lbtrip.domain.recommendation.dto.response.RegionRecommendationResponse;
import live.lbtrip.domain.recommendation.service.RecommendationGenerationService;
import live.lbtrip.domain.recommendation.service.RecommendationService;
import live.lbtrip.global.web.UserId;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/recommendations")
public class RecommendationController implements RecommendationApi {

    private final RecommendationGenerationService recommendationGenerationService;
    private final RecommendationService recommendationService;

    @PostMapping
    public ResponseEntity<Void> createRecommendations(@UserId Long userId) {
        recommendationGenerationService.createRecommendations(userId);
        return ResponseEntity.status(CREATED).build();
    }

    @GetMapping("/regions")
    public ResponseEntity<List<RegionRecommendationResponse>> getRecommendedRegions(@UserId Long userId) {
        List<RegionRecommendationResponse> responses = recommendationService.getRecommendedRegions(userId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/regions/{regionId}/courses")
    public ResponseEntity<List<CourseCandidateResponse>> getRegionCourses(
        @UserId Long userId,
        @PathVariable Long regionId
    ) {
        List<CourseCandidateResponse> responses = recommendationService.getRegionCourses(userId, regionId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/courses/{courseId}")
    public ResponseEntity<CourseDetailResponse> getCourseDetail(
        @UserId Long userId,
        @PathVariable Long courseId
    ) {
        CourseDetailResponse response = recommendationService.getCourseDetail(userId, courseId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/courses/{courseId}/save")
    public ResponseEntity<Void> saveCourse(
        @UserId Long userId,
        @PathVariable Long courseId
    ) {
        recommendationService.saveCourse(userId, courseId);
        return ResponseEntity.status(CREATED).build();
    }
}
