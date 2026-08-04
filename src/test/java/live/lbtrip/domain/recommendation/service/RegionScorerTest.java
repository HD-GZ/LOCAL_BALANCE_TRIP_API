package live.lbtrip.domain.recommendation.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import live.lbtrip.domain.propensity.model.Preference;
import live.lbtrip.domain.propensity.model.Propensity;
import live.lbtrip.domain.propensity.model.ValueConsumption;
import live.lbtrip.domain.tourism.client.dto.RegionStats;
import live.lbtrip.support.fixture.UserFixture;

class RegionScorerTest {

    private final RegionScorer regionScorer = new RegionScorer();

    @Test
    void 로컬_선호가_높으면_관광지_수가_적은_지역을_우선한다() {
        Propensity propensity = Propensity.create(
            UserFixture.user(),
            Preference.of(5, 3, 3, 3, 3),
            ValueConsumption.of(3, 3, 3, 3, 3));
        RegionStats popular = stats("인기 지역", 100);
        RegionStats local = stats("로컬 지역", 10);

        List<RegionStats> selected = regionScorer.selectTop(propensity, List.of(popular, local), 1);

        assertThat(selected).singleElement().extracting(RegionStats::regionName).isEqualTo("로컬 지역");
    }

    @Test
    void 중립_성향의_동점은_입력_순서를_유지한다() {
        Propensity propensity = Propensity.create(
            UserFixture.user(),
            Preference.of(3, 3, 3, 3, 3),
            ValueConsumption.of(3, 3, 3, 3, 3));
        List<RegionStats> stats = List.of(stats("첫 지역", 10), stats("두 번째 지역", 20));

        List<RegionStats> selected = regionScorer.selectTop(propensity, stats, 2);

        assertThat(selected).extracting(RegionStats::regionName)
            .containsExactly("첫 지역", "두 번째 지역");
    }

    private RegionStats stats(String name, int totalCount) {
        return new RegionStats(name, "46", "710", totalCount, 10, Map.of());
    }
}
