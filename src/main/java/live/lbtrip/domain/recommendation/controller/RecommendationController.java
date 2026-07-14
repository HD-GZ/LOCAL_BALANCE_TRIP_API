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
import live.lbtrip.domain.recommendation.service.RecommendationService;
import live.lbtrip.global.web.UserId;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/recommendations")
public class RecommendationController implements RecommendationApi {

    private final RecommendationService recommendationService;

    @PostMapping
    public ResponseEntity<Void> createRecommendations(@UserId Long userId) {
        recommendationService.createRecommendations(userId);
        return ResponseEntity.status(CREATED).build();
    }

    @GetMapping("/regions")
    public ResponseEntity<List<RegionRecommendationResponse>> getRecommendedRegions(@UserId Long userId) {
        return ResponseEntity.ok(recommendationService.getRecommendedRegions(userId));
    }

    @GetMapping("/regions/{regionId}/courses")
    public ResponseEntity<List<CourseCandidateResponse>> getRegionCourses(
        @UserId Long userId,
        @PathVariable Long regionId
    ) {
        return ResponseEntity.ok(recommendationService.getRegionCourses(userId, regionId));
    }

    @GetMapping("/courses/{courseId}")
    public ResponseEntity<CourseDetailResponse> getCourseDetail(
        @UserId Long userId,
        @PathVariable Long courseId
    ) {
        return ResponseEntity.ok(recommendationService.getCourseDetail(userId, courseId));
    }

    @PostMapping("/courses/{courseId}/save")
    public ResponseEntity<Void> saveCourse(
        @UserId Long userId,
        @PathVariable Long courseId
    ) {
        return ResponseEntity.status(CREATED).build();
    }
}
