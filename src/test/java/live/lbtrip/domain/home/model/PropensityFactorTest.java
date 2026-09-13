package live.lbtrip.domain.home.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import live.lbtrip.domain.propensity.model.Preference;
import live.lbtrip.domain.propensity.model.ValueConsumption;

class PropensityFactorTest {

    @Test
    void 열_개의_요소를_가진다() {
        assertThat(PropensityFactor.values()).hasSize(10);
    }

    @Test
    void preference_요소의_점수를_추출한다() {
        Preference preference = Preference.of(4, 5, 3, 2, 1);
        ValueConsumption vc = ValueConsumption.of(1, 2, 3, 4, 5);

        assertThat(PropensityFactor.LOCALITY.score(preference, vc)).isEqualTo(4);
        assertThat(PropensityFactor.SOCIALITY.score(preference, vc)).isEqualTo(1);
    }

    @Test
    void valueConsumption_요소의_점수를_추출한다() {
        Preference preference = Preference.of(4, 5, 3, 2, 1);
        ValueConsumption vc = ValueConsumption.of(1, 2, 3, 4, 5);

        assertThat(PropensityFactor.ACCOMMODATION.score(preference, vc)).isEqualTo(1);
        assertThat(PropensityFactor.CAFE_EXHIBITION.score(preference, vc)).isEqualTo(5);
    }

    @Test
    void 각_요소는_min_max_라벨_메시지_키를_가진다() {
        assertThat(PropensityFactor.LOCALITY.minLabelKey()).isEqualTo("propensityFactor.LOCALITY.min");
        assertThat(PropensityFactor.LOCALITY.maxLabelKey()).isEqualTo("propensityFactor.LOCALITY.max");
    }
}
