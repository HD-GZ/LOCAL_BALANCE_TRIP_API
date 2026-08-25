package live.lbtrip.domain.tourism.model.enums;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TourContentType {

    TOURIST_SPOT(12, 76, "관광지", "Tourist Attraction", true),
    CULTURAL_FACILITY(14, 78, "문화시설", "Cultural Facility", true),
    LEPORTS(28, 75, "레포츠", "Leisure Sports", true),
    ACCOMMODATION(32, 80, "숙박", "Accommodation", false),
    SHOPPING(38, 79, "쇼핑", "Shopping", true),
    RESTAURANT(39, 82, "음식점", "Restaurant", true),
    FESTIVAL(15, 85, "축제공연행사", "Festival", false);

    private static final String UNKNOWN_NAME = "기타";
    private static final String UNKNOWN_ENGLISH_NAME = "Other";

    private final int code;
    private final int engCode;
    private final String koreanName;
    private final String englishName;
    private final boolean courseCandidate;

    public static List<TourContentType> courseCandidates() {
        return Arrays.stream(values()).filter(TourContentType::isCourseCandidate).toList();
    }

    public int codeFor(Locale locale) {
        return isEnglish(locale) ? engCode : code;
    }

    public static String koreanNameOf(int code) {
        return nameOf(code, Locale.KOREAN);
    }

    public static String nameOf(int code, Locale locale) {
        boolean english = isEnglish(locale);
        return Arrays.stream(values())
            .filter(type -> type.code == code)
            .findFirst()
            .map(type -> english ? type.englishName : type.koreanName)
            .orElse(english ? UNKNOWN_ENGLISH_NAME : UNKNOWN_NAME);
    }

    private static boolean isEnglish(Locale locale) {
        return Locale.ENGLISH.getLanguage().equals(locale.getLanguage());
    }
}
