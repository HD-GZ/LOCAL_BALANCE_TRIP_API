package live.lbtrip.domain.tourism.model.enums;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Locale;

import org.junit.jupiter.api.Test;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class TourContentTypeTest {

    @ParameterizedTest
    @CsvSource({
        "TOURIST_SPOT, 12, 76",
        "CULTURAL_FACILITY, 14, 78",
        "LEPORTS, 28, 75",
        "ACCOMMODATION, 32, 80",
        "SHOPPING, 38, 79",
        "RESTAURANT, 39, 82",
        "FESTIVAL, 15, 85"
    })
    void 한국어_코드와_영문_코드를_함께_가진다(TourContentType type, int code, int engCode) {
        assertThat(type.getCode()).isEqualTo(code);
        assertThat(type.getEngCode()).isEqualTo(engCode);
    }

    @Test
    void 축제는_코스_후보에서_제외된다() {
        assertThat(TourContentType.courseCandidates())
            .doesNotContain(TourContentType.FESTIVAL, TourContentType.ACCOMMODATION);
        assertThat(TourContentType.nameOf(15, Locale.KOREAN)).isEqualTo("축제공연행사");
        assertThat(TourContentType.nameOf(15, Locale.ENGLISH)).isEqualTo("Festival");
    }

    @Test
    void 로케일에_맞는_유형명을_돌려준다() {
        assertThat(TourContentType.nameOf(39, Locale.KOREAN)).isEqualTo("음식점");
        assertThat(TourContentType.nameOf(39, Locale.ENGLISH)).isEqualTo("Restaurant");
        assertThat(TourContentType.nameOf(99, Locale.KOREAN)).isEqualTo("기타");
        assertThat(TourContentType.nameOf(99, Locale.ENGLISH)).isEqualTo("Other");
    }

    @Test
    void 로케일에_맞는_TourAPI_코드를_돌려준다() {
        assertThat(TourContentType.TOURIST_SPOT.codeFor(Locale.KOREAN)).isEqualTo(12);
        assertThat(TourContentType.TOURIST_SPOT.codeFor(Locale.ENGLISH)).isEqualTo(76);
        assertThat(TourContentType.TOURIST_SPOT.codeFor(Locale.US)).isEqualTo(76);
    }
}
