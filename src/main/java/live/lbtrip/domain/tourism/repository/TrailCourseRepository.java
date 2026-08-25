package live.lbtrip.domain.tourism.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import live.lbtrip.domain.tourism.model.entity.TrailCourse;

public interface TrailCourseRepository extends JpaRepository<TrailCourse, Long> {

    Optional<TrailCourse> findByCrsIdx(String crsIdx);

    @Query("select t from TrailCourse t where t.regionCandidate.id = :regionCandidateId order by t.distanceKm asc")
    List<TrailCourse> findByRegionCandidateIdOrderByDistance(Long regionCandidateId, Pageable pageable);

    @Query("select t.regionCandidate.id from TrailCourse t where t.regionCandidate is not null group by t.regionCandidate.id")
    List<Long> findRegionCandidateIdsWithCourses();
}
