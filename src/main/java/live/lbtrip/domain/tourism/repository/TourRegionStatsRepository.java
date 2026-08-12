package live.lbtrip.domain.tourism.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import live.lbtrip.domain.tourism.model.entity.TourRegionStats;

public interface TourRegionStatsRepository extends JpaRepository<TourRegionStats, Long> {

    Optional<TourRegionStats> findByRegionCandidateId(Long regionCandidateId);

    @Query("SELECT s FROM TourRegionStats s JOIN FETCH s.regionCandidate")
    List<TourRegionStats> findAllWithRegionCandidate();
}
