package live.lbtrip.domain.recommendation.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import live.lbtrip.domain.recommendation.model.entity.SavedCourse;

public interface SavedCourseRepository extends JpaRepository<SavedCourse, Long> {

    boolean existsByUserIdAndSourceCourseId(Long userId, Long sourceCourseId);
}
