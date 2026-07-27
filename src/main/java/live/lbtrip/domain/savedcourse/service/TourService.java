package live.lbtrip.domain.savedcourse.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import live.lbtrip.domain.savedcourse.dto.response.TourProgressResponse;
import live.lbtrip.domain.savedcourse.dto.response.TourSummaryResponse;
import live.lbtrip.domain.savedcourse.model.entity.SavedCourse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TourService {

    private final SavedCourseFinder savedCourseFinder;

    @Transactional
    public TourProgressResponse startTour(Long userId, Long savedCourseId) {
        SavedCourse savedCourse = savedCourseFinder.findByIdAndUserId(savedCourseId, userId);
        savedCourse.startTour();

        return TourProgressResponse.from(savedCourse);
    }

    @Transactional
    public void checkIn(Long userId, Long savedCourseId, Long placeId) {
        SavedCourse savedCourse = savedCourseFinder.findByIdAndUserId(savedCourseId, userId);
        savedCourse.checkInPlace(placeId);
    }

    @Transactional
    public TourSummaryResponse endTour(Long userId, Long savedCourseId) {
        SavedCourse savedCourse = savedCourseFinder.findByIdAndUserId(savedCourseId, userId);
        boolean completed = savedCourse.endTour();

        return TourSummaryResponse.of(
            completed,
            savedCourse.countVisitedPlaces(),
            savedCourse.getPlaces().size(),
            savedCourse.tourDurationMinutes()
        );
    }
}
