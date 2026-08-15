package live.lbtrip.domain.tourism.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import live.lbtrip.domain.tourism.model.entity.RegionVisitorStats;
import live.lbtrip.domain.tourism.model.enums.VisitorType;
import live.lbtrip.domain.tourism.repository.dto.RegionVisitorSum;

public interface RegionVisitorStatsRepository extends JpaRepository<RegionVisitorStats, Long> {

    Optional<RegionVisitorStats> findByRegionCandidateIdAndBaseDateAndVisitorType(
        Long regionCandidateId, LocalDate baseDate, VisitorType visitorType);

    boolean existsByBaseDate(LocalDate baseDate);

    Optional<RegionVisitorStats> findFirstByOrderByBaseDateDesc();

    @Query("""
        SELECT v.regionCandidate.id AS regionCandidateId, SUM(v.visitorCount) AS total
        FROM RegionVisitorStats v
        WHERE v.visitorType = :visitorType AND v.baseDate > :after
        GROUP BY v.regionCandidate.id
        """)
    List<RegionVisitorSum> sumByRegionCandidate(VisitorType visitorType, LocalDate after);
}
