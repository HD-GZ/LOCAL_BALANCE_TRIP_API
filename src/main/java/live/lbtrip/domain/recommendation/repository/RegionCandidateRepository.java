package live.lbtrip.domain.recommendation.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import live.lbtrip.domain.recommendation.model.entity.RegionCandidate;

public interface RegionCandidateRepository extends JpaRepository<RegionCandidate, Long> {

    boolean existsByLdongRegnCdAndLdongSignguCd(String ldongRegnCd, String ldongSignguCd);
}
