package live.lbtrip.domain.recommendation.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import live.lbtrip.domain.recommendation.model.entity.RecommendedRegion;
import live.lbtrip.domain.recommendation.repository.dto.PopularRegionCode;

public interface RecommendedRegionRepository extends JpaRepository<RecommendedRegion, Long> {

    List<RecommendedRegion> findAllByUserIdOrderByDisplayOrder(Long userId);

    Optional<RecommendedRegion> findByIdAndUserId(Long id, Long userId);

    Optional<RecommendedRegion> findFirstByLdongRegnCdAndLdongSignguCd(String ldongRegnCd, String ldongSignguCd);

    @Query(
        "SELECT r.ldongRegnCd AS ldongRegnCd, r.ldongSignguCd AS ldongSignguCd "
            + "FROM RecommendedRegion r "
            + "WHERE r.ldongRegnCd IS NOT NULL AND r.ldongSignguCd IS NOT NULL "
            + "GROUP BY r.ldongRegnCd, r.ldongSignguCd "
            + "ORDER BY COUNT(r) DESC, r.ldongRegnCd ASC, r.ldongSignguCd ASC")
    List<PopularRegionCode> findPopularRegionCodes(Pageable pageable);
}
