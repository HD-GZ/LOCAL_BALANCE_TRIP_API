package live.lbtrip.domain.savedcourse.controller;

import static org.springframework.http.HttpStatus.CREATED;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import live.lbtrip.domain.savedcourse.service.SavedCourseService;
import live.lbtrip.global.web.UserId;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/recommendations/courses")
@RequiredArgsConstructor
public class CourseSaveController implements CourseSaveApi {

    private final SavedCourseService savedCourseService;

    @PostMapping("/{courseId}/save")
    public ResponseEntity<Void> saveCourse(
        @UserId Long userId,
        @PathVariable Long courseId
    ) {
        savedCourseService.saveCourse(userId, courseId);
        return ResponseEntity.status(CREATED).build();
    }
}
