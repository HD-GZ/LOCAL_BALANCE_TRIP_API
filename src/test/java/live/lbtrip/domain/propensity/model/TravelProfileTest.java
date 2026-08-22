package live.lbtrip.domain.propensity.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Locale;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import live.lbtrip.support.fixture.TravelProfileFixture;

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
    }

    @Nested
    class 로케일별_텍스트_조회 {

        @Test
        void 영어_로케일이면_영문_별칭과_설명을_반환한다() {
            TravelProfile travelProfile = TravelProfileFixture.travelProfileWithEnglish();

            assertThat(travelProfile.nicknameFor(Locale.ENGLISH)).isEqualTo(TravelProfileFixture.NICKNAME_EN);
            assertThat(travelProfile.descriptionFor(Locale.ENGLISH)).isEqualTo(TravelProfileFixture.DESCRIPTION_EN);
        }

        @Test
        void 한국어_로케일이면_한글_별칭과_설명을_반환한다() {
            TravelProfile travelProfile = TravelProfileFixture.travelProfileWithEnglish();

            assertThat(travelProfile.nicknameFor(Locale.KOREAN)).isEqualTo(TravelProfileFixture.NICKNAME);
            assertThat(travelProfile.descriptionFor(Locale.KOREAN)).isEqualTo(TravelProfileFixture.DESCRIPTION);
        }

        @Test
        void 영문_번역이_없으면_한글로_폴백한다() {
            TravelProfile travelProfile = TravelProfileFixture.travelProfile();

            assertThat(travelProfile.nicknameFor(Locale.ENGLISH)).isEqualTo(TravelProfileFixture.NICKNAME);
            assertThat(travelProfile.descriptionFor(Locale.ENGLISH)).isEqualTo(TravelProfileFixture.DESCRIPTION);
        }
    }
}
