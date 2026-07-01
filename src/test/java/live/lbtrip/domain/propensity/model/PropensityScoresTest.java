package live.lbtrip.domain.propensity.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import live.lbtrip.support.fixture.PropensityFixture;
import live.lbtrip.support.fixture.PropensityRequestFixture;

class PropensityScoresTest {

    @Nested
    class 변환 {

        @Test
        void 요청에서_점수_값으로_변환한다() {
            PropensityScores scores = PropensityScores.from(PropensityRequestFixture.propensityRequest());

            assertThat(scores.locality()).isEqualTo(PropensityRequestFixture.LOCALITY);
            assertThat(scores.frugality()).isEqualTo(PropensityRequestFixture.FRUGALITY);
            assertThat(scores.experientiality()).isEqualTo(PropensityRequestFixture.EXPERIENTIALITY);
            assertThat(scores.vitality()).isEqualTo(PropensityRequestFixture.VITALITY);
            assertThat(scores.sociality()).isEqualTo(PropensityRequestFixture.SOCIALITY);
            assertThat(scores.accommodation()).isEqualTo(PropensityRequestFixture.ACCOMMODATION);
            assertThat(scores.food()).isEqualTo(PropensityRequestFixture.FOOD);
            assertThat(scores.experience()).isEqualTo(PropensityRequestFixture.EXPERIENCE);
            assertThat(scores.transportation()).isEqualTo(PropensityRequestFixture.TRANSPORTATION);
            assertThat(scores.cafeExhibition()).isEqualTo(PropensityRequestFixture.CAFE_EXHIBITION);
        }

        @Test
        void 엔티티에서_점수_값으로_변환한다() {
            Propensity propensity = PropensityFixture.propensity();

            PropensityScores scores = PropensityScores.from(propensity);

            assertThat(scores.locality()).isEqualTo(PropensityRequestFixture.LOCALITY);
            assertThat(scores.frugality()).isEqualTo(PropensityRequestFixture.FRUGALITY);
            assertThat(scores.experientiality()).isEqualTo(PropensityRequestFixture.EXPERIENTIALITY);
            assertThat(scores.vitality()).isEqualTo(PropensityRequestFixture.VITALITY);
            assertThat(scores.sociality()).isEqualTo(PropensityRequestFixture.SOCIALITY);
            assertThat(scores.accommodation()).isEqualTo(PropensityRequestFixture.ACCOMMODATION);
            assertThat(scores.food()).isEqualTo(PropensityRequestFixture.FOOD);
            assertThat(scores.experience()).isEqualTo(PropensityRequestFixture.EXPERIENCE);
            assertThat(scores.transportation()).isEqualTo(PropensityRequestFixture.TRANSPORTATION);
            assertThat(scores.cafeExhibition()).isEqualTo(PropensityRequestFixture.CAFE_EXHIBITION);
        }
    }
}
