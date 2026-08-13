package live.lbtrip.domain.tourism.model.enums;

import java.util.EnumSet;
import java.util.Set;

public enum CategoryGroup {

    LUXURY_SHOPPING(
        Set.of(),
        Set.of(),
        Set.of("A04010300", "A04010400")
    ),
    TRADITIONAL_MARKET(
        Set.of(),
        Set.of(),
        Set.of("A04010100", "A04010200", "A04010900")
    ),
    VIEWING_PLACE(
        Set.of(),
        Set.of("A0201", "A0205", "A0206"),
        Set.of()
    ),
    EXPERIENCE_PLACE(
        Set.of(),
        Set.of("A0203"),
        Set.of("A04010700")
    ),
    NATURE_REST(
        Set.of("A01"),
        Set.of("A0202"),
        Set.of()
    ),
    CAFE(
        Set.of(),
        Set.of(),
        Set.of("A05020900")
    ),
    EXHIBITION(
        Set.of(),
        Set.of(),
        Set.of("A02060100", "A02060300", "A02060500")
    );

    private final Set<String> cat1Codes;
    private final Set<String> cat2Codes;
    private final Set<String> cat3Codes;

    CategoryGroup(Set<String> cat1Codes, Set<String> cat2Codes, Set<String> cat3Codes) {
        this.cat1Codes = cat1Codes;
        this.cat2Codes = cat2Codes;
        this.cat3Codes = cat3Codes;
    }

    public static Set<CategoryGroup> classify(String cat1, String cat2, String cat3) {
        Set<CategoryGroup> groups = EnumSet.noneOf(CategoryGroup.class);
        for (CategoryGroup group : values()) {
            if (group.matches(cat1, cat2, cat3)) {
                groups.add(group);
            }
        }
        return groups;
    }

    private boolean matches(String cat1, String cat2, String cat3) {
        return contains(cat1Codes, cat1)
            || contains(cat2Codes, cat2)
            || contains(cat3Codes, cat3);
    }

    private static boolean contains(Set<String> codes, String code) {
        return code != null && codes.contains(code);
    }
}
