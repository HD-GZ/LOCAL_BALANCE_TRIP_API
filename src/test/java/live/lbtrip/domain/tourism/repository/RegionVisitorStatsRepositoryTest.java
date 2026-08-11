package live.lbtrip.domain.tourism.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import live.lbtrip.domain.tourism.model.entity.RegionVisitorStats;
import live.lbtrip.domain.tourism.model.enums.VisitorType;

@DataJpaTest
class RegionVisitorStatsRepositoryTest {

    @Autowired
    private RegionVisitorStatsRepository regionVisitorStatsRepository;

    @Test
    void 기간_이후_외지인_방문자수를_합산한다() {
        LocalDate latest = LocalDate.of(2026, 7, 10);
        regionVisitorStatsRepository.save(
            RegionVisitorStats.create("46", "710", latest, VisitorType.OUTSIDER, 100.5));
        regionVisitorStatsRepository.save(
            RegionVisitorStats.create("46", "710", latest.minusDays(1), VisitorType.OUTSIDER, 50.0));
        regionVisitorStatsRepository.save(
            RegionVisitorStats.create("46", "710", latest, VisitorType.LOCAL, 999.0));
        regionVisitorStatsRepository.save(
            RegionVisitorStats.create("46", "710", latest.minusDays(40), VisitorType.OUTSIDER, 777.0));

        double sum = regionVisitorStatsRepository.sumVisitors(
            "46", "710", VisitorType.OUTSIDER, latest.minusDays(30));

        assertThat(sum).isEqualTo(150.5);
    }

    @Test
    void 데이터가_없으면_합산은_0이다() {
        double sum = regionVisitorStatsRepository.sumVisitors(
            "46", "710", VisitorType.OUTSIDER, LocalDate.of(2026, 7, 1));

        assertThat(sum).isZero();
    }

    @Test
    void 가장_최근_기준일을_조회한다() {
        regionVisitorStatsRepository.save(RegionVisitorStats.create(
            "46", "710", LocalDate.of(2026, 7, 9), VisitorType.OUTSIDER, 1.0));
        regionVisitorStatsRepository.save(RegionVisitorStats.create(
            "46", "710", LocalDate.of(2026, 7, 10), VisitorType.OUTSIDER, 1.0));

        assertThat(regionVisitorStatsRepository.findMaxBaseDate())
            .contains(LocalDate.of(2026, 7, 10));
        assertThat(regionVisitorStatsRepository.existsByBaseDate(LocalDate.of(2026, 7, 10))).isTrue();
        assertThat(regionVisitorStatsRepository.existsByBaseDate(LocalDate.of(2026, 7, 11))).isFalse();
    }
}
