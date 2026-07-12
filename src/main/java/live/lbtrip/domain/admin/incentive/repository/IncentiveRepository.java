package live.lbtrip.domain.admin.incentive.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import live.lbtrip.domain.admin.incentive.model.Incentive;

public interface IncentiveRepository extends JpaRepository<Incentive, Long> {
}
