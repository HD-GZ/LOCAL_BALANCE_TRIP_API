package live.lbtrip.domain.region.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import live.lbtrip.domain.region.model.RegionGreenMetrics;

public interface RegionGreenMetricsRepository extends JpaRepository<RegionGreenMetrics, Long> {

    @Query("select g from RegionGreenMetrics g join fetch g.regionCandidate")
    List<RegionGreenMetrics> findAllWithRegionCandidate();
}
