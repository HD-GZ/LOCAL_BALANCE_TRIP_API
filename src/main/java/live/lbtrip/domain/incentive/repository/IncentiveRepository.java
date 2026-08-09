package live.lbtrip.domain.incentive.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import live.lbtrip.domain.incentive.model.Incentive;

public interface IncentiveRepository extends JpaRepository<Incentive, Long> {

    @Query("""
        SELECT DISTINCT i
        FROM Incentive i
        JOIN i.regions r
        WHERE r.ldongRegnCd = :ldongRegnCd
          AND r.ldongSignguCd = :ldongSignguCd
        ORDER BY i.id ASC
        """)
    List<Incentive> findAllByRegion(
        @Param("ldongRegnCd") String ldongRegnCd,
        @Param("ldongSignguCd") String ldongSignguCd
    );

    @Query("""
        SELECT DISTINCT i
        FROM Incentive i
        JOIN i.regions r
        WHERE r.ldongRegnCd = :ldongRegnCd
          AND r.ldongSignguCd = :ldongSignguCd
          AND i.startDate <= :today
          AND (i.endDate IS NULL OR i.endDate >= :today)
        ORDER BY CASE WHEN i.endDate IS NULL THEN 1 ELSE 0 END ASC, i.endDate ASC, i.id ASC
        """)
    List<Incentive> findActiveByRegion(
        @Param("ldongRegnCd") String ldongRegnCd,
        @Param("ldongSignguCd") String ldongSignguCd,
        @Param("today") LocalDate today
    );
}
