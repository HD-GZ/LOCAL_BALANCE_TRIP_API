package live.lbtrip.domain.tourism.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import live.lbtrip.domain.tourism.model.entity.RegionVisitorStats;
import live.lbtrip.domain.tourism.model.enums.VisitorType;

public interface RegionVisitorStatsRepository extends JpaRepository<RegionVisitorStats, Long> {

    Optional<RegionVisitorStats> findByLdongRegnCdAndLdongSignguCdAndBaseDateAndVisitorType(
        String ldongRegnCd, String ldongSignguCd, LocalDate baseDate, VisitorType visitorType);

    boolean existsByBaseDate(LocalDate baseDate);

    Optional<RegionVisitorStats> findFirstByOrderByBaseDateDesc();

    List<RegionVisitorStats> findAllByLdongRegnCdAndLdongSignguCdAndVisitorTypeAndBaseDateAfter(
        String ldongRegnCd,
        String ldongSignguCd,
        VisitorType visitorType,
        LocalDate after
    );
}
