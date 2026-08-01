package live.lbtrip.domain.propensity.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import live.lbtrip.domain.propensity.model.TravelProfile;

public interface TravelProfileRepository extends JpaRepository<TravelProfile, Long> {

    Optional<TravelProfile> findByCode(String code);
}
