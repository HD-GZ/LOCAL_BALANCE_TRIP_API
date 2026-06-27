package live.lbtrip.domain.propensity.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import live.lbtrip.domain.propensity.model.Propensity;

public interface PropensityRepository extends JpaRepository<Propensity, Long> {

    Optional<Propensity> findByUserId(Long userId);
}
