package live.lbtrip.domain.tourism.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import live.lbtrip.domain.tourism.model.entity.CategoryGroupCode;
import live.lbtrip.domain.tourism.model.enums.CategoryGroup;
import live.lbtrip.domain.tourism.model.enums.CodeLevel;
import live.lbtrip.domain.tourism.model.vo.CategoryGroupMapping;
import live.lbtrip.domain.tourism.repository.CategoryGroupCodeRepository;

@ExtendWith(MockitoExtension.class)
class CategoryGroupClassifierTest {

    @Mock
    private CategoryGroupCodeRepository categoryGroupCodeRepository;

    @InjectMocks
    private CategoryGroupClassifier categoryGroupClassifier;

    private CategoryGroupMapping mapping;

    @BeforeEach
    void setUp() {
        when(categoryGroupCodeRepository.findAll()).thenReturn(List.of(
            CategoryGroupCode.create(CategoryGroup.LUXURY_SHOPPING, CodeLevel.CAT3, "A04010300"),
            CategoryGroupCode.create(CategoryGroup.TRADITIONAL_MARKET, CodeLevel.CAT3, "A04010100"),
            CategoryGroupCode.create(CategoryGroup.VIEWING_PLACE, CodeLevel.CAT2, "A0206"),
            CategoryGroupCode.create(CategoryGroup.EXPERIENCE_PLACE, CodeLevel.CAT2, "A0203"),
            CategoryGroupCode.create(CategoryGroup.EXPERIENCE_PLACE, CodeLevel.CAT3, "A04010700"),
            CategoryGroupCode.create(CategoryGroup.NATURE_REST, CodeLevel.CAT1, "A01"),
            CategoryGroupCode.create(CategoryGroup.CAFE, CodeLevel.CAT3, "A05020900"),
            CategoryGroupCode.create(CategoryGroup.EXHIBITION, CodeLevel.CAT3, "A02060100")));
        mapping = categoryGroupClassifier.load();
    }

    @Test
    void cat3_코드로_그룹을_분류한다() {
        assertThat(mapping.classify("A04", "A0401", "A04010300"))
            .containsExactly(CategoryGroup.LUXURY_SHOPPING);
        assertThat(mapping.classify("A04", "A0401", "A04010100"))
            .containsExactly(CategoryGroup.TRADITIONAL_MARKET);
        assertThat(mapping.classify("A05", "A0502", "A05020900"))
            .containsExactly(CategoryGroup.CAFE);
    }

    @Test
    void cat2와_cat1_코드로도_그룹을_분류한다() {
        assertThat(mapping.classify("A02", "A0203", "A02030200"))
            .containsExactly(CategoryGroup.EXPERIENCE_PLACE);
        assertThat(mapping.classify("A01", "A0101", "A01010100"))
            .containsExactly(CategoryGroup.NATURE_REST);
    }

    @Test
    void 박물관은_관람_시설이면서_전시_시설이다() {
        assertThat(mapping.classify("A02", "A0206", "A02060100"))
            .containsExactlyInAnyOrder(CategoryGroup.VIEWING_PLACE, CategoryGroup.EXHIBITION);
    }

    @Test
    void 어느_그룹에도_속하지_않으면_빈_집합을_반환한다() {
        assertThat(mapping.classify("A05", "A0502", "A05020100")).isEmpty();
        assertThat(mapping.classify(null, null, null)).isEmpty();
        assertThat(mapping.classify("", "", "")).isEmpty();
    }
}
