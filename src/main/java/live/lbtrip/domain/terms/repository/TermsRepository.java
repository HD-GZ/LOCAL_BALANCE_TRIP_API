package live.lbtrip.domain.terms.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import live.lbtrip.domain.terms.model.Terms;
import live.lbtrip.domain.terms.model.TermsType;

public interface TermsRepository extends JpaRepository<Terms, Long> {

    Optional<Terms> findFirstByTypeAndEffectiveDateLessThanEqualOrderByEffectiveDateDescIdDesc(
        TermsType type,
        LocalDate baseDate
    );
}
