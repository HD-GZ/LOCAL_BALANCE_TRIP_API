package live.lbtrip.domain.savedcourse.tour.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import live.lbtrip.domain.savedcourse.tour.dto.response.TourProgressResponse;
import live.lbtrip.domain.savedcourse.tour.dto.response.TourSummaryResponse;
import live.lbtrip.domain.savedcourse.tour.service.TourService;
import live.lbtrip.global.web.UserId;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class TourController implements TourApi {

    private final TourService tourService;

    @PostMapping("/saved-courses/{savedCourseId}/tour/start")
    public ResponseEntity<TourProgressResponse> startTour(
        @UserId Long userId,
        @PathVariable Long savedCourseId
    ) {
        TourProgressResponse response = tourService.startTour(userId, savedCourseId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/saved-courses/{savedCourseId}/tour/places/{placeId}/check-in")
    public ResponseEntity<Void> checkIn(
        @UserId Long userId,
        @PathVariable Long savedCourseId,
        @PathVariable Long placeId
    ) {
        tourService.checkIn(userId, savedCourseId, placeId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/saved-courses/{savedCourseId}/tour/end")
    public ResponseEntity<TourSummaryResponse> endTour(
        @UserId Long userId,
        @PathVariable Long savedCourseId
    ) {
        TourSummaryResponse response = tourService.endTour(userId, savedCourseId);
        return ResponseEntity.ok(response);
    }
}
