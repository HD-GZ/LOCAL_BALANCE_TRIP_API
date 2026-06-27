package live.lbtrip.global.util;

import java.util.Locale;

public final class StringNormalizer {

    private StringNormalizer() {
    }

    public static String trim(String value) {
        return value.trim();
    }

    public static String trimToLowerCase(String value) {
        return trim(value).toLowerCase(Locale.ROOT);
    }
}
