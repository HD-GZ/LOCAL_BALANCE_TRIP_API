package live.lbtrip.domain.admin.incentive.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import live.lbtrip.domain.admin.incentive.model.Incentive;

public interface IncentiveRepository extends JpaRepository<Incentive, Long> {

    @Query("""
        select distinct i
        from Incentive i
        join i.regions r
        where r.ldongRegnCd = :ldongRegnCd
          and r.ldongSignguCd = :ldongSignguCd
        order by i.id asc
        """)
    List<Incentive> findAllByRegion(
        @Param("ldongRegnCd") String ldongRegnCd,
        @Param("ldongSignguCd") String ldongSignguCd
    );
}
