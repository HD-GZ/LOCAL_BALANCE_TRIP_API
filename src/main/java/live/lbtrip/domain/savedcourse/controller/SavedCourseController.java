package live.lbtrip.domain.savedcourse.controller;

import static org.springframework.http.HttpStatus.CREATED;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import live.lbtrip.domain.savedcourse.dto.request.SavedCourseStatusUpdateRequest;
import live.lbtrip.domain.savedcourse.dto.response.SavedCourseDetailResponse;
import live.lbtrip.domain.savedcourse.dto.response.SavedCourseListResponse;
import live.lbtrip.domain.savedcourse.model.enums.SavedCourseStatus;
import live.lbtrip.domain.savedcourse.service.SavedCourseService;
import live.lbtrip.global.web.PageQueryRequest;
import live.lbtrip.global.web.UserId;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class SavedCourseController implements SavedCourseApi {

    private final SavedCourseService savedCourseService;

    @PostMapping("/recommendations/courses/{courseId}/save")
    public ResponseEntity<Void> saveCourse(
        @UserId Long userId,
        @PathVariable Long courseId
    ) {
        savedCourseService.saveCourse(userId, courseId);
        return ResponseEntity.status(CREATED).build();
    }

    @GetMapping("/saved-courses")
    public ResponseEntity<SavedCourseListResponse> getSavedCourses(
        @UserId Long userId,
        @RequestParam(required = false) SavedCourseStatus status,
        @Valid @ModelAttribute PageQueryRequest request
    ) {
        SavedCourseListResponse response = savedCourseService.getSavedCourses(userId, status, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/saved-courses/{savedCourseId}")
    public ResponseEntity<SavedCourseDetailResponse> getSavedCourseDetail(
        @UserId Long userId,
        @PathVariable Long savedCourseId
    ) {
        SavedCourseDetailResponse response = savedCourseService.getSavedCourseDetail(userId, savedCourseId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/saved-courses/{savedCourseId}/status")
    public ResponseEntity<Void> updateSavedCourseStatus(
        @UserId Long userId,
        @PathVariable Long savedCourseId,
        @Valid @RequestBody SavedCourseStatusUpdateRequest request
    ) {
        savedCourseService.updateStatus(userId, savedCourseId, request.status());
        return ResponseEntity.ok().build();
    }
}
