package live.lbtrip.domain.tourism.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import live.lbtrip.domain.tourism.model.entity.OdiiTheme;

public interface OdiiThemeRepository extends JpaRepository<OdiiTheme, Long> {

    Optional<OdiiTheme> findByTidAndTlid(String tid, String tlid);

    List<OdiiTheme> findAllByAudioSyncedAtIsNull();

    List<OdiiTheme> findAllByLongitudeBetweenAndLatitudeBetween(
        double minLongitude, double maxLongitude, double minLatitude, double maxLatitude);
}
