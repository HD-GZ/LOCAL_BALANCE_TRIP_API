package live.lbtrip.domain.recommendation.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import live.lbtrip.domain.recommendation.model.entity.RecommendedRegion;
import live.lbtrip.domain.recommendation.repository.dto.PopularRegion;

public interface RecommendedRegionRepository extends JpaRepository<RecommendedRegion, Long> {

    List<RecommendedRegion> findAllByUserIdOrderByDisplayOrder(Long userId);

    Optional<RecommendedRegion> findByIdAndUserId(Long id, Long userId);

    Optional<RecommendedRegion> findFirstByRegionCandidateId(Long regionCandidateId);

    @Query(
        "SELECT r.regionCandidate.id AS regionCandidateId "
            + "FROM RecommendedRegion r "
            + "GROUP BY r.regionCandidate.id "
            + "ORDER BY COUNT(r) DESC, r.regionCandidate.id ASC")
    List<PopularRegion> findPopularRegions(Pageable pageable);
}
