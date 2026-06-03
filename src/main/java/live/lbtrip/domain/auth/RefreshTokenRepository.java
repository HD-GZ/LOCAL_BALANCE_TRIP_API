package live.lbtrip.domain.auth;

import java.util.Optional;

import live.lbtrip.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

	Optional<RefreshToken> findByToken(String token);

	void deleteByUser(User user);
}
