package live.lbtrip.domain.savedcourse.service;

import org.springframework.stereotype.Component;

import live.lbtrip.domain.recommendation.model.entity.CoursePlace;
import live.lbtrip.domain.recommendation.model.entity.GeneratedCourse;
import live.lbtrip.domain.recommendation.model.entity.RecommendedRegion;
import live.lbtrip.domain.savedcourse.model.entity.SavedCourse;
import live.lbtrip.domain.savedcourse.model.entity.SavedCoursePlace;
import live.lbtrip.domain.savedcourse.repository.SavedCourseRepository;
import live.lbtrip.domain.user.model.User;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SaveCourseManager {

    private final SavedCourseRepository savedCourseRepository;

    public void add(GeneratedCourse generatedCourse, User user) {
        RecommendedRegion recommendedRegion = generatedCourse.getRecommendedRegion();
        SavedCourse savedCourse = SavedCourse.create(
            user,
            generatedCourse.getId(),
            generatedCourse.getName(),
            recommendedRegion.getRegionName(),
            generatedCourse.getReason(),
            generatedCourse.getImageUrl(),
            recommendedRegion.getLdongRegnCd(),
            recommendedRegion.getLdongSignguCd()
        );

        for (CoursePlace place : generatedCourse.getPlaces()) {
            savedCourse.addPlace(SavedCoursePlace.create(
                place.getVisitOrder(),
                place.getName(),
                place.getOverview(),
                place.getImageUrl(),
                place.getLatitude(),
                place.getLongitude(),
                place.getWalkMinutes(),
                place.isHasAudio(),
                place.getAudioUrl()
                )
            );
        }
        savedCourseRepository.save(savedCourse);
    }
}
