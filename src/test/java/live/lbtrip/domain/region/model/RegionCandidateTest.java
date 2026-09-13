package live.lbtrip.domain.region.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Locale;

import org.junit.jupiter.api.Test;

class RegionCandidateTest {

    @Test
    void 지역_후보를_생성한다() {
        RegionCandidate candidate = RegionCandidate.create("전라남도 담양군", "46", "710");

        assertThat(candidate.getName()).isEqualTo("전라남도 담양군");
        assertThat(candidate.getLdongRegnCd()).isEqualTo("46");
        assertThat(candidate.getLdongSignguCd()).isEqualTo("710");
        assertThat(candidate.getNameEn()).isNull();
    }

    @Test
    void 영문명이_있으면_영문_로케일에_영문명을_돌려준다() {
        RegionCandidate candidate = RegionCandidate.create("전라남도 담양군", "46", "710");
        candidate.updateNameEn("Damyang-gun, Jeollanam-do");

        assertThat(candidate.nameFor(Locale.ENGLISH)).isEqualTo("Damyang-gun, Jeollanam-do");
        assertThat(candidate.nameFor(Locale.US)).isEqualTo("Damyang-gun, Jeollanam-do");
        assertThat(candidate.nameFor(Locale.KOREAN)).isEqualTo("전라남도 담양군");
    }

    @Test
    void 영문명이_없으면_영문_로케일에도_한국어명으로_폴백한다() {
        RegionCandidate candidate = RegionCandidate.create("전라남도 담양군", "46", "710");

        assertThat(candidate.nameFor(Locale.ENGLISH)).isEqualTo("전라남도 담양군");
    }
}
