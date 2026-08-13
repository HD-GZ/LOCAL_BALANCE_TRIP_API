package live.lbtrip.domain.tourism.model.vo;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import live.lbtrip.domain.tourism.model.enums.CategoryGroup;

public record CategoryGroupMapping(
    Map<String, Set<CategoryGroup>> byCat1,
    Map<String, Set<CategoryGroup>> byCat2,
    Map<String, Set<CategoryGroup>> byCat3
) {

    public static CategoryGroupMapping of(
        Map<String, Set<CategoryGroup>> byCat1,
        Map<String, Set<CategoryGroup>> byCat2,
        Map<String, Set<CategoryGroup>> byCat3
    ) {
        return new CategoryGroupMapping(byCat1, byCat2, byCat3);
    }

    public Set<CategoryGroup> classify(String cat1, String cat2, String cat3) {
        Set<CategoryGroup> groups = EnumSet.noneOf(CategoryGroup.class);
        groups.addAll(byCat1.getOrDefault(nullToEmpty(cat1), Set.of()));
        groups.addAll(byCat2.getOrDefault(nullToEmpty(cat2), Set.of()));
        groups.addAll(byCat3.getOrDefault(nullToEmpty(cat3), Set.of()));
        return groups;
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
