package live.lbtrip.domain.tourism.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import live.lbtrip.domain.tourism.model.entity.RegionVisitorStats;
import live.lbtrip.domain.tourism.model.enums.VisitorType;

public interface RegionVisitorStatsRepository extends JpaRepository<RegionVisitorStats, Long> {

    Optional<RegionVisitorStats> findByLdongRegnCdAndLdongSignguCdAndBaseDateAndVisitorType(
        String ldongRegnCd, String ldongSignguCd, LocalDate baseDate, VisitorType visitorType);

    boolean existsByBaseDate(LocalDate baseDate);

    @Query("select max(v.baseDate) from RegionVisitorStats v")
    Optional<LocalDate> findMaxBaseDate();

    @Query("""
        select coalesce(sum(v.visitorCount), 0)
        from RegionVisitorStats v
        where v.ldongRegnCd = :ldongRegnCd
          and v.ldongSignguCd = :ldongSignguCd
          and v.visitorType = :visitorType
          and v.baseDate > :after
        """)
    double sumVisitors(
        @Param("ldongRegnCd") String ldongRegnCd,
        @Param("ldongSignguCd") String ldongSignguCd,
        @Param("visitorType") VisitorType visitorType,
        @Param("after") LocalDate after);
}
