package live.lbtrip.domain.tourism.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import live.lbtrip.domain.tourism.model.entity.RegionVisitorStats;
import live.lbtrip.domain.tourism.model.enums.VisitorType;
import live.lbtrip.domain.tourism.repository.RegionVisitorStatsRepository;

@ExtendWith(MockitoExtension.class)
class RegionVisitorFinderTest {

    @Mock
    private RegionVisitorStatsRepository regionVisitorStatsRepository;

    @InjectMocks
    private RegionVisitorFinder regionVisitorFinder;

    @Test
    void 최근_가용일_기준_30일_외지인_방문자를_합산한다() {
        LocalDate latest = LocalDate.of(2026, 7, 10);
        RegionVisitorStats latestStats = RegionVisitorStats.create(
            "46", "710", latest, VisitorType.OUTSIDER, 1000.0);
        when(regionVisitorStatsRepository.findFirstByOrderByBaseDateDesc())
            .thenReturn(Optional.of(latestStats));
        when(regionVisitorStatsRepository
            .findAllByLdongRegnCdAndLdongSignguCdAndVisitorTypeAndBaseDateAfter(
                "46", "710", VisitorType.OUTSIDER, latest.minusDays(30)))
            .thenReturn(List.of(
                latestStats,
                RegionVisitorStats.create("46", "710", latest.minusDays(1), VisitorType.OUTSIDER, 234.5)
            ));

        double sum = regionVisitorFinder.sumRecentOutsiderVisitors("46", "710");

        assertThat(sum).isEqualTo(1234.5);
        verify(regionVisitorStatsRepository)
            .findAllByLdongRegnCdAndLdongSignguCdAndVisitorTypeAndBaseDateAfter(
                "46", "710", VisitorType.OUTSIDER, latest.minusDays(30));
    }

    @Test
    void 방문자_데이터가_전혀_없으면_0을_반환한다() {
        when(regionVisitorStatsRepository.findFirstByOrderByBaseDateDesc()).thenReturn(Optional.empty());

        assertThat(regionVisitorFinder.sumRecentOutsiderVisitors("46", "710")).isZero();
    }
}
