package live.lbtrip.domain.recommendation.service;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import live.lbtrip.domain.recommendation.model.entity.CoursePlace;
import live.lbtrip.domain.recommendation.model.entity.GeneratedCourse;
import live.lbtrip.domain.recommendation.model.entity.RecommendedRegion;
import live.lbtrip.domain.recommendation.model.vo.RegionPlan;
import live.lbtrip.domain.recommendation.repository.RecommendedRegionRepository;
import live.lbtrip.domain.user.model.User;
import live.lbtrip.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RecommendationStore {

    private final RecommendedRegionRepository recommendedRegionRepository;
    private final UserRepository userRepository;

    @Transactional
    public void replace(Long userId, List<RegionPlan> plans) {
        List<RecommendedRegion> existing = recommendedRegionRepository.findAllByUserIdOrderByDisplayOrder(userId);
        recommendedRegionRepository.deleteAll(existing);
        recommendedRegionRepository.flush();

        User userRef = userRepository.getReferenceById(userId);
        int displayOrder = 1;
        for (RegionPlan plan : plans) {
            RecommendedRegion region = RecommendedRegion.create(
                userRef, plan.regionName(), plan.ldongRegnCd(), plan.ldongSignguCd(),
                plan.imageUrl(), plan.reason(), displayOrder++);

            int courseDisplayOrder = 1;
            for (RegionPlan.CoursePlanData courseData : plan.courses()) {
                GeneratedCourse course = GeneratedCourse.create(
                    userRef, courseData.name(), courseData.reason(), courseData.imageUrl(), courseDisplayOrder++);
                region.addCourse(course);

                for (RegionPlan.PlaceSnapshot place : courseData.places()) {
                    course.addPlace(CoursePlace.create(
                        place.visitOrder(), place.name(), place.overview(), place.imageUrl(),
                        place.latitude(), place.longitude(), place.walkMinutes(),
                        place.hasAudio(), place.audioUrl()));
                }
            }
            recommendedRegionRepository.save(region);
        }
    }
}
