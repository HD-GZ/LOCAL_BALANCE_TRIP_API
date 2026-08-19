package live.lbtrip.domain.tourism.model.enums;

import static org.assertj.core.api.Assertions.assertThat;

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
        "RESTAURANT, 39, 82"
    })
    void 한국어_코드와_영문_코드를_함께_가진다(TourContentType type, int code, int engCode) {
        assertThat(type.getCode()).isEqualTo(code);
        assertThat(type.getEngCode()).isEqualTo(engCode);
    }
}
