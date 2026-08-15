package live.lbtrip.domain.recommendation.service;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import live.lbtrip.domain.recommendation.model.entity.CoursePlace;
import live.lbtrip.domain.recommendation.model.entity.GeneratedCourse;
import live.lbtrip.domain.recommendation.model.entity.RecommendedRegion;
import live.lbtrip.domain.recommendation.model.vo.RegionPlan;
import live.lbtrip.domain.recommendation.model.vo.RegionPlan.PlannedCourse;
import live.lbtrip.domain.recommendation.model.vo.RoutedPlace;
import live.lbtrip.domain.recommendation.repository.RecommendedRegionRepository;
import live.lbtrip.domain.region.model.RegionCandidate;
import live.lbtrip.domain.region.repository.RegionCandidateRepository;
import live.lbtrip.domain.tourism.model.entity.TourPlace;
import live.lbtrip.domain.user.model.User;
import live.lbtrip.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RecommendationStore {

    private final RecommendedRegionRepository recommendedRegionRepository;
    private final UserRepository userRepository;
    private final RegionCandidateRepository regionCandidateRepository;

    @Transactional
    public void replace(Long userId, List<RegionPlan> plans) {
        recommendedRegionRepository.deleteAll(recommendedRegionRepository.findAllByUserIdOrderByDisplayOrder(userId));
        recommendedRegionRepository.flush();

        User user = userRepository.getReferenceById(userId);
        int regionOrder = 1;
        for (RegionPlan plan : plans) {
            RegionCandidate regionCandidate = regionCandidateRepository.getReferenceById(plan.region().regionCandidateId());
            RecommendedRegion region = RecommendedRegion.create(
                user,
                plan.region().regionName(),
                regionCandidate,
                firstPlaceImageUrl(plan.courses().getFirst()),
                plan.regionReason(),
                regionOrder++
            );

            int courseOrder = 1;
            for (PlannedCourse plannedCourse : plan.courses()) {
                GeneratedCourse course = GeneratedCourse.create(
                    user,
                    plannedCourse.name(),
                    plannedCourse.reason(),
                    firstPlaceImageUrl(plannedCourse),
                    courseOrder++
                );
                region.addCourse(course);

                int visitOrder = 1;
                for (RoutedPlace routedPlace : plannedCourse.places()) {
                    TourPlace place = routedPlace.place();
                    String audioUrl = place.getOdiiTheme() == null ? null : place.getOdiiTheme().getAudioUrl();
                    course.addPlace(CoursePlace.create(
                        visitOrder++,
                        place.getTitle(),
                        place.getOverview(),
                        place.getImageUrl(),
                        place.getLatitude(),
                        place.getLongitude(),
                        routedPlace.walkMinutes(),
                        audioUrl != null,
                        audioUrl
                        )
                    );
                }
            }
            recommendedRegionRepository.save(region);
        }
    }

    private String firstPlaceImageUrl(PlannedCourse course) {
        return course.places().getFirst().place().getImageUrl();
    }
}
