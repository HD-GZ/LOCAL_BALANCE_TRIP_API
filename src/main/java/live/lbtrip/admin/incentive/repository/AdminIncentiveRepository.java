package live.lbtrip.admin.incentive.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import live.lbtrip.domain.incentive.model.Incentive;

public interface AdminIncentiveRepository extends JpaRepository<Incentive, Long> {
}
