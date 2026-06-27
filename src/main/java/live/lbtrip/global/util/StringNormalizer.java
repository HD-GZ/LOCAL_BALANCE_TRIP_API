package live.lbtrip.global.util;

import java.util.Locale;

public final class StringNormalizer {

    private StringNormalizer() {
    }

    public static String trimToLowerCase(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
