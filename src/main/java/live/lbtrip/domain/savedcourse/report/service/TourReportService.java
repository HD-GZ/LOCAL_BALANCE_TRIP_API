package live.lbtrip.domain.savedcourse.report.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import live.lbtrip.domain.savedcourse.course.service.SavedCourseFinder;
import live.lbtrip.domain.savedcourse.model.entity.SavedCourse;
import live.lbtrip.domain.savedcourse.report.dto.response.TourReportResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TourReportService {

    private final SavedCourseFinder savedCourseFinder;

    public TourReportResponse getReport(Long userId, Long savedCourseId) {
        SavedCourse savedCourse = savedCourseFinder.findByIdAndUserId(savedCourseId, userId);
        savedCourse.validateReportAvailable();

        return TourReportResponse.from(savedCourse);
    }
}
