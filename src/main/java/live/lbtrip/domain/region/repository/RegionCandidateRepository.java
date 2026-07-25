package live.lbtrip.domain.region.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import live.lbtrip.domain.region.model.RegionCandidate;

public interface RegionCandidateRepository extends JpaRepository<RegionCandidate, Long> {

    boolean existsByLdongRegnCdAndLdongSignguCd(String ldongRegnCd, String ldongSignguCd);
}
