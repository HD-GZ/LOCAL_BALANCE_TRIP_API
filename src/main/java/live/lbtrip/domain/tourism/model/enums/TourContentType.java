package live.lbtrip.domain.tourism.model.enums;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TourContentType {

    TOURIST_SPOT(12, 76, "관광지", true),
    CULTURAL_FACILITY(14, 78, "문화시설", true),
    LEPORTS(28, 75, "레포츠", true),
    ACCOMMODATION(32, 80, "숙박", false),
    SHOPPING(38, 79, "쇼핑", true),
    RESTAURANT(39, 82, "음식점", true);

    private static final String UNKNOWN_NAME = "기타";

    private final int code;
    private final int engCode;
    private final String koreanName;
    private final boolean courseCandidate;

    public static List<TourContentType> courseCandidates() {
        return Arrays.stream(values()).filter(TourContentType::isCourseCandidate).toList();
    }

    public int codeFor(Locale locale) {
        return Locale.ENGLISH.getLanguage().equals(locale.getLanguage()) ? engCode : code;
    }

    public static String koreanNameOf(int code) {
        return Arrays.stream(values())
            .filter(type -> type.code == code)
            .findFirst()
            .map(TourContentType::getKoreanName)
            .orElse(UNKNOWN_NAME);
    }
}
