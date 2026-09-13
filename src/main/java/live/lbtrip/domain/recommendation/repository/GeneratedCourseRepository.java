package live.lbtrip.domain.recommendation.repository;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import live.lbtrip.domain.recommendation.model.entity.GeneratedCourse;

public interface GeneratedCourseRepository extends JpaRepository<GeneratedCourse, Long> {

    Optional<GeneratedCourse> findByIdAndUserIdAndRecommendedRegionLocale(Long id, Long userId, Locale locale);

    Optional<GeneratedCourse> findByIdAndRecommendedRegionLocale(Long id, Locale locale);

    List<GeneratedCourse> findAllByUserId(Long userId);

    Optional<GeneratedCourse> findFirstByRecommendedRegionRegionCandidateIdAndRecommendedRegionLocaleOrderByIdAsc(
        Long regionCandidateId, Locale locale);
}
