package live.lbtrip.domain.recommendation.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import live.lbtrip.domain.recommendation.model.entity.GeneratedCourse;

public interface GeneratedCourseRepository extends JpaRepository<GeneratedCourse, Long> {

    Optional<GeneratedCourse> findByIdAndUserId(Long id, Long userId);

    List<GeneratedCourse> findAllByUserId(Long userId);
}
