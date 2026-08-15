package live.lbtrip.domain.home.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.Test;

import live.lbtrip.domain.home.model.PropensityFactor;

class PropensityFactorSelectorTest {

    @Test
    void 서로_다른_세_개를_선택한다() {
        PropensityFactorSelector selector = new PropensityFactorSelector(new Random(42));

        List<PropensityFactor> selected = selector.selectThree();

        assertThat(selected).hasSize(3);
        assertThat(selected).doesNotHaveDuplicates();
    }

    @Test
    void 시드가_같으면_동일하게_선택한다() {
        List<PropensityFactor> first = new PropensityFactorSelector(new Random(1)).selectThree();
        List<PropensityFactor> second = new PropensityFactorSelector(new Random(1)).selectThree();

        assertThat(first).isEqualTo(second);
    }
}
