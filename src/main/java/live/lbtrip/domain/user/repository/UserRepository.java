package live.lbtrip.domain.user.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import live.lbtrip.domain.user.model.User;
import live.lbtrip.domain.user.model.UserStatus;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    List<User> findAllByStatusAndDeletedAtIsNullAndWithdrawnAtBefore(UserStatus status, LocalDateTime cutoff);
}
