package live.lbtrip.domain.savedcourse.report.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import live.lbtrip.domain.savedcourse.report.dto.response.TourReportResponse;
import live.lbtrip.domain.savedcourse.report.service.TourReportService;
import live.lbtrip.global.web.UserId;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class TourReportController implements TourReportApi {

    private final TourReportService tourReportService;

    @GetMapping("/saved-courses/{savedCourseId}/report")
    public ResponseEntity<TourReportResponse> getReport(
        @UserId Long userId,
        @PathVariable Long savedCourseId
    ) {
        TourReportResponse response = tourReportService.getReport(userId, savedCourseId);
        return ResponseEntity.ok(response);
    }
}
