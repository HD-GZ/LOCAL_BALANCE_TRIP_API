package live.lbtrip.domain.propensity.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class TravelProfileTest {

    @Nested
    class 생성 {

        @Test
        void 여행_프로필을_생성한다() {
            TravelProfile travelProfile = TravelProfile.create(
                PropensityBucket.HIGH,
                PropensityBucket.HIGH,
                PropensityBucket.LOW,
                PropensityBucket.LOW,
                PropensityBucket.LOW,
                PropensityBucket.LOW,
                PropensityBucket.HIGH,
                PropensityBucket.HIGH,
                PropensityBucket.LOW,
                PropensityBucket.NEUTRAL,
                "실속형 로컬 감성 여행자",
                "럭셔리보다 실속을 즐기는 여행자예요."
            );

            assertThat(travelProfile.getLocalityBucket()).isEqualTo(PropensityBucket.HIGH);
            assertThat(travelProfile.getFrugalityBucket()).isEqualTo(PropensityBucket.HIGH);
            assertThat(travelProfile.getExperientialityBucket()).isEqualTo(PropensityBucket.LOW);
            assertThat(travelProfile.getVitalityBucket()).isEqualTo(PropensityBucket.LOW);
            assertThat(travelProfile.getSocialityBucket()).isEqualTo(PropensityBucket.LOW);
            assertThat(travelProfile.getAccommodationBucket()).isEqualTo(PropensityBucket.LOW);
            assertThat(travelProfile.getFoodBucket()).isEqualTo(PropensityBucket.HIGH);
            assertThat(travelProfile.getExperienceBucket()).isEqualTo(PropensityBucket.HIGH);
            assertThat(travelProfile.getTransportationBucket()).isEqualTo(PropensityBucket.LOW);
            assertThat(travelProfile.getCafeExhibitionBucket()).isEqualTo(PropensityBucket.NEUTRAL);
            assertThat(travelProfile.getType()).isEqualTo("실속형 로컬 감성 여행자");
            assertThat(travelProfile.getDescription()).isEqualTo("럭셔리보다 실속을 즐기는 여행자예요.");
        }
    }
}
