package live.lbtrip.domain.incentive.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import live.lbtrip.domain.incentive.model.Incentive;
import live.lbtrip.domain.incentive.model.IncentiveRegion;
import live.lbtrip.domain.region.model.RegionCandidate;

@DataJpaTest
class IncentiveActiveQueryTest {

    @Autowired
    private IncentiveRepository incentiveRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void 진행중이거나_상시인_인센티브만_마감일_오름차순으로_조회한다() {
        LocalDate today = LocalDate.now();
        RegionCandidate candidate = RegionCandidate.create("전라남도 담양군", "46", "710");
        entityManager.persist(candidate);

        Incentive active = incentive(candidate, "진행중 인센티브",
            today.minusDays(5), today.plusDays(10));
        Incentive alwaysOn = incentive(candidate, "상시 인센티브",
            today.minusDays(1), null);
        Incentive expired = incentive(candidate, "종료된 인센티브",
            today.minusDays(20), today.minusDays(1));
        Incentive future = incentive(candidate, "예정된 인센티브",
            today.plusDays(5), today.plusDays(20));

        incentiveRepository.save(active);
        incentiveRepository.save(alwaysOn);
        incentiveRepository.save(expired);
        incentiveRepository.save(future);
        entityManager.flush();
        entityManager.clear();

        List<Incentive> result = incentiveRepository.findActiveByRegion(candidate.getId(), today);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Incentive::getTitle)
            .containsExactly("진행중 인센티브", "상시 인센티브");
    }

    private Incentive incentive(RegionCandidate candidate, String title, LocalDate startDate, LocalDate endDate) {
        Incentive incentive = Incentive.create(title, "https://event.example.com", "설명", startDate, endDate);
        incentive.replaceRegions(List.of(IncentiveRegion.create(candidate)));
        return incentive;
    }
}
