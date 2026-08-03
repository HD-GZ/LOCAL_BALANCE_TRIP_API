package live.lbtrip.domain.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import live.lbtrip.domain.auth.model.SignupVerificationToken;

public interface SignupVerificationTokenRepository extends JpaRepository<SignupVerificationToken, Long> {

    Optional<SignupVerificationToken> findByCode(String code);

    boolean existsByCode(String code);
}
