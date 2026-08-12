package live.lbtrip.domain.savedcourse.share.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import live.lbtrip.domain.savedcourse.share.model.entity.CourseShareToken;

public interface CourseShareTokenRepository extends JpaRepository<CourseShareToken, Long> {

    Optional<CourseShareToken> findByToken(String token);
}
