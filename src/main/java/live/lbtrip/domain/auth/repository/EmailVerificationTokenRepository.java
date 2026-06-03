package live.lbtrip.domain.auth.repository;

import java.util.Optional;

import live.lbtrip.domain.auth.model.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {

	Optional<EmailVerificationToken> findByCode(String code);

	boolean existsByCode(String code);
}
