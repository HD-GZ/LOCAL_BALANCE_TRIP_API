package live.lbtrip.domain.tourism.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import live.lbtrip.domain.region.model.RegionCandidate;
import live.lbtrip.domain.region.repository.RegionCandidateRepository;
import live.lbtrip.domain.tourism.model.entity.RegionVisitorStats;
import live.lbtrip.domain.tourism.model.enums.VisitorType;
import live.lbtrip.support.fixture.RegionCandidateFixture;

@DataJpaTest
class RegionVisitorStatsRepositoryTest {

    @Autowired
    private RegionCandidateRepository regionCandidateRepository;

    @Autowired
    private RegionVisitorStatsRepository regionVisitorStatsRepository;

    @Test
    void 기간_이후_외지인_방문자수를_조회한다() {
        RegionCandidate candidate = regionCandidateRepository.save(RegionCandidateFixture.candidate());
        LocalDate latest = LocalDate.of(2026, 7, 10);
        regionVisitorStatsRepository.save(
            RegionVisitorStats.create(candidate, latest, VisitorType.OUTSIDER, 100.5));
        regionVisitorStatsRepository.save(
            RegionVisitorStats.create(candidate, latest.minusDays(1), VisitorType.OUTSIDER, 50.0));
        regionVisitorStatsRepository.save(
            RegionVisitorStats.create(candidate, latest, VisitorType.LOCAL, 999.0));
        regionVisitorStatsRepository.save(
            RegionVisitorStats.create(candidate, latest.minusDays(40), VisitorType.OUTSIDER, 777.0));

        List<RegionVisitorStats> visitorStats = regionVisitorStatsRepository
            .findAllByRegionCandidateIdAndVisitorTypeAndBaseDateAfter(
                candidate.getId(), VisitorType.OUTSIDER, latest.minusDays(30));

        assertThat(visitorStats)
            .extracting(RegionVisitorStats::getVisitorCount)
            .containsExactlyInAnyOrder(100.5, 50.0);
    }

    @Test
    void 조건에_맞는_데이터가_없으면_빈_목록을_반환한다() {
        RegionCandidate candidate = regionCandidateRepository.save(RegionCandidateFixture.candidate());

        List<RegionVisitorStats> visitorStats = regionVisitorStatsRepository
            .findAllByRegionCandidateIdAndVisitorTypeAndBaseDateAfter(
                candidate.getId(), VisitorType.OUTSIDER, LocalDate.of(2026, 7, 1));

        assertThat(visitorStats).isEmpty();
    }

    @Test
    void 가장_최근_기준일을_조회한다() {
        RegionCandidate candidate = regionCandidateRepository.save(RegionCandidateFixture.candidate());
        regionVisitorStatsRepository.save(RegionVisitorStats.create(
            candidate, LocalDate.of(2026, 7, 9), VisitorType.OUTSIDER, 1.0));
        regionVisitorStatsRepository.save(RegionVisitorStats.create(
            candidate, LocalDate.of(2026, 7, 10), VisitorType.OUTSIDER, 1.0));

        assertThat(regionVisitorStatsRepository.findFirstByOrderByBaseDateDesc())
            .map(RegionVisitorStats::getBaseDate)
            .contains(LocalDate.of(2026, 7, 10));
        assertThat(regionVisitorStatsRepository.existsByBaseDate(LocalDate.of(2026, 7, 10))).isTrue();
        assertThat(regionVisitorStatsRepository.existsByBaseDate(LocalDate.of(2026, 7, 11))).isFalse();
    }
}
