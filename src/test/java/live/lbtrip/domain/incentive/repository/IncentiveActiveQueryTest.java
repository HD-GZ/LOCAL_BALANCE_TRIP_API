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

@DataJpaTest
class IncentiveActiveQueryTest {

    private static final String LDONG_REGN_CD = "46";
    private static final String LDONG_SIGNGU_CD = "710";

    @Autowired
    private IncentiveRepository incentiveRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void 진행중이거나_상시인_인센티브만_마감일_오름차순으로_조회한다() {
        LocalDate today = LocalDate.now();

        Incentive active = incentive("진행중 인센티브",
            today.minusDays(5), today.plusDays(10));
        Incentive alwaysOn = incentive("상시 인센티브",
            today.minusDays(1), null);
        Incentive expired = incentive("종료된 인센티브",
            today.minusDays(20), today.minusDays(1));
        Incentive future = incentive("예정된 인센티브",
            today.plusDays(5), today.plusDays(20));

        incentiveRepository.save(active);
        incentiveRepository.save(alwaysOn);
        incentiveRepository.save(expired);
        incentiveRepository.save(future);
        entityManager.flush();
        entityManager.clear();

        List<Incentive> result = incentiveRepository.findActiveByRegion(LDONG_REGN_CD, LDONG_SIGNGU_CD, today);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Incentive::getTitle)
            .containsExactly("진행중 인센티브", "상시 인센티브");
    }

    private Incentive incentive(String title, LocalDate startDate, LocalDate endDate) {
        Incentive incentive = Incentive.create(title, "https://event.example.com", "설명", startDate, endDate);
        incentive.replaceRegions(List.of(IncentiveRegion.create(LDONG_REGN_CD, LDONG_SIGNGU_CD)));
        return incentive;
    }
}
