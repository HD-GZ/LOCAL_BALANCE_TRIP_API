package live.lbtrip.domain.tourism.service;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import live.lbtrip.domain.tourism.model.entity.TrailCourse;
import live.lbtrip.domain.tourism.repository.TrailCourseRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TrailCourseFinder {

    public static final int MAX_TRAILS = 5;

    private final TrailCourseRepository trailCourseRepository;

    public List<TrailCourse> findByRegion(Long regionCandidateId) {
        if (regionCandidateId == null) {
            return List.of();
        }
        return trailCourseRepository.findByRegionCandidateIdOrderByDistance(
            regionCandidateId, PageRequest.of(0, MAX_TRAILS));
    }
}
