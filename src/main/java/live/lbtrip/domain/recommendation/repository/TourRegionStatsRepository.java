package live.lbtrip.domain.recommendation.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import live.lbtrip.domain.recommendation.model.entity.TourRegionStats;

public interface TourRegionStatsRepository extends JpaRepository<TourRegionStats, Long> {

    Optional<TourRegionStats> findByLdongRegnCdAndLdongSignguCd(String ldongRegnCd, String ldongSignguCd);
}
