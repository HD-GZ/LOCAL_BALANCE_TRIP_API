package live.lbtrip.domain.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import live.lbtrip.domain.auth.model.PasswordResetToken;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findFirstByUserIdAndCodeOrderByIdDesc(Long userId, String code);

    Optional<PasswordResetToken> findByResetToken(String resetToken);

    void deleteByUserId(Long userId);
}
