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
                "LVEAI",
                "찐로컬 탐험가",
                "로컬의 구석구석을 발로 뛰며 직접 체험하는 실속파 혼행 여행자예요.",
                "travel-profiles/lveai.png"
            );

            assertThat(travelProfile.getCode()).isEqualTo("LVEAI");
            assertThat(travelProfile.getNickname()).isEqualTo("찐로컬 탐험가");
            assertThat(travelProfile.getDescription()).isEqualTo("로컬의 구석구석을 발로 뛰며 직접 체험하는 실속파 혼행 여행자예요.");
            assertThat(travelProfile.getImageKey()).isEqualTo("travel-profiles/lveai.png");
        }

        @Test
        void 이미지_키_없이_여행_프로필을_생성한다() {
            TravelProfile travelProfile = TravelProfile.create(
                "HPSRI",
                "프라이빗 무드 컬렉터",
                "유명한 곳일수록 특별하게, 아낌없이 누리는 나만의 시간을 사랑하는 여행자예요.",
                null
            );

            assertThat(travelProfile.getImageKey()).isNull();
        }
    }
}
