package live.lbtrip.domain.home.model;

import java.util.function.ToIntBiFunction;

import live.lbtrip.domain.propensity.model.Preference;
import live.lbtrip.domain.propensity.model.ValueConsumption;
import lombok.Getter;

@Getter
public enum PropensityFactor {

    LOCALITY("핫플·유명 명소", "로컬·골목 상권", (p, v) -> p.getLocality()),
    FRUGALITY("럭셔리·프리미엄", "실속·가성비", (p, v) -> p.getFrugality()),
    EXPERIENTIALITY("관람형·보고 즐기기", "생활 체험·직접 해보기", (p, v) -> p.getExperientiality()),
    VITALITY("휴식형·느긋한 쉼", "활동형·부지런한 일정", (p, v) -> p.getVitality()),
    SOCIALITY("혼행·나 홀로", "세대 동행·가족", (p, v) -> p.getSociality()),
    ACCOMMODATION("숙소 아끼기", "숙소 투자", (p, v) -> v.getAccommodation()),
    FOOD("음식 아끼기", "음식 투자", (p, v) -> v.getFood()),
    EXPERIENCE("체험 아끼기", "체험 투자", (p, v) -> v.getExperience()),
    TRANSPORTATION("이동 아끼기", "이동 투자", (p, v) -> v.getTransportation()),
    CAFE_EXHIBITION("카페·전시 아끼기", "카페·전시 투자", (p, v) -> v.getCafeExhibition());

    private final String minLabel;
    private final String maxLabel;
    private final ToIntBiFunction<Preference, ValueConsumption> extractor;

    PropensityFactor(String minLabel, String maxLabel, ToIntBiFunction<Preference, ValueConsumption> extractor) {
        this.minLabel = minLabel;
        this.maxLabel = maxLabel;
        this.extractor = extractor;
    }

    public int score(Preference preference, ValueConsumption valueConsumption) {
        return extractor.applyAsInt(preference, valueConsumption);
    }
}
