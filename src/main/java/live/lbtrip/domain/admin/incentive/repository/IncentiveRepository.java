package live.lbtrip.domain.admin.incentive.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import live.lbtrip.domain.admin.incentive.model.Incentive;

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
}
