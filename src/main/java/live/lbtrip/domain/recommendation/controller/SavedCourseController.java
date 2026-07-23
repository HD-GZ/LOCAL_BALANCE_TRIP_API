package live.lbtrip.domain.recommendation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import live.lbtrip.domain.recommendation.dto.request.SavedCourseListRequest;
import live.lbtrip.domain.recommendation.dto.response.SavedCourseDetailResponse;
import live.lbtrip.domain.recommendation.dto.response.SavedCourseListResponse;
import live.lbtrip.domain.recommendation.service.SavedCourseService;
import live.lbtrip.global.web.UserId;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/saved-courses")
@RequiredArgsConstructor
public class SavedCourseController implements SavedCourseApi {

    private final SavedCourseService savedCourseService;

    @GetMapping
    public ResponseEntity<SavedCourseListResponse> getSavedCourses(
        @UserId Long userId,
        @Valid @ModelAttribute SavedCourseListRequest request
    ) {
        SavedCourseListResponse response = savedCourseService.getSavedCourses(
            userId, request.page(), request.size());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{savedCourseId}")
    public ResponseEntity<SavedCourseDetailResponse> getSavedCourseDetail(
        @UserId Long userId,
        @PathVariable Long savedCourseId
    ) {
        SavedCourseDetailResponse response = savedCourseService.getSavedCourseDetail(userId, savedCourseId);
        return ResponseEntity.ok(response);
    }
}
