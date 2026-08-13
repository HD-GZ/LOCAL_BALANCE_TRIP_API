package live.lbtrip.domain.tourism.model.enums;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CategoryGroupTest {

    @ParameterizedTest
    @CsvSource({
        "A04010300, LUXURY_SHOPPING",
        "A04010400, LUXURY_SHOPPING",
        "A04010100, TRADITIONAL_MARKET",
        "A04010200, TRADITIONAL_MARKET",
        "A04010900, TRADITIONAL_MARKET",
        "A04010700, EXPERIENCE_PLACE",
        "A05020900, CAFE",
        "A02060100, EXHIBITION",
        "A02060300, EXHIBITION",
        "A02060500, EXHIBITION"
    })
    void cat3_코드로_그룹을_분류한다(String cat3, CategoryGroup expected) {
        assertThat(CategoryGroup.classify(null, null, cat3)).containsExactly(expected);
    }

    @ParameterizedTest
    @CsvSource({
        "A0201, VIEWING_PLACE",
        "A0205, VIEWING_PLACE",
        "A0206, VIEWING_PLACE",
        "A0203, EXPERIENCE_PLACE",
        "A0202, NATURE_REST"
    })
    void cat2_코드로_그룹을_분류한다(String cat2, CategoryGroup expected) {
        assertThat(CategoryGroup.classify(null, cat2, null)).containsExactly(expected);
    }

    @Test
    void cat1_코드로_그룹을_분류한다() {
        assertThat(CategoryGroup.classify("A01", null, null))
            .containsExactly(CategoryGroup.NATURE_REST);
    }

    @Test
    void 박물관은_관람_시설이면서_전시_시설이다() {
        assertThat(CategoryGroup.classify("A02", "A0206", "A02060100"))
            .containsExactlyInAnyOrder(CategoryGroup.VIEWING_PLACE, CategoryGroup.EXHIBITION);
    }

    @Test
    void 어느_그룹에도_속하지_않으면_빈_집합을_반환한다() {
        assertThat(CategoryGroup.classify("A05", "A0502", "A05020100")).isEmpty();
        assertThat(CategoryGroup.classify(null, null, null)).isEmpty();
        assertThat(CategoryGroup.classify("", "", "")).isEmpty();
    }
}
