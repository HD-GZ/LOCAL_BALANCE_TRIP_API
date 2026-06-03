package live.lbtrip.domain.auth.repository;

import java.util.Optional;

import live.lbtrip.domain.auth.model.RefreshToken;
import live.lbtrip.domain.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

	Optional<RefreshToken> findByToken(String token);

	void deleteByUser(User user);
}
