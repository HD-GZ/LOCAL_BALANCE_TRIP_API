package live.lbtrip.domain.savedcourse.share.controller;

import static org.springframework.http.HttpStatus.CREATED;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import live.lbtrip.domain.savedcourse.course.dto.response.SavedCourseDetailResponse;
import live.lbtrip.domain.savedcourse.share.dto.response.ShareTokenResponse;
import live.lbtrip.domain.savedcourse.share.service.CourseShareService;
import live.lbtrip.global.web.UserId;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class CourseShareController implements CourseShareApi {

    private final CourseShareService courseShareService;

    @PostMapping("/saved-courses/{savedCourseId}/share-tokens")
    public ResponseEntity<ShareTokenResponse> issueShareToken(
        @UserId Long userId,
        @PathVariable Long savedCourseId
    ) {
        ShareTokenResponse response = courseShareService.issueShareToken(userId, savedCourseId);
        return ResponseEntity.status(CREATED).body(response);
    }

    @GetMapping("/shared-courses/{token}")
    public ResponseEntity<SavedCourseDetailResponse> getSharedCourseDetail(
        @PathVariable String token
    ) {
        SavedCourseDetailResponse response = courseShareService.getSharedCourseDetail(token);
        return ResponseEntity.ok(response);
    }
}
