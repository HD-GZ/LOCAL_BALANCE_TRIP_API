package live.lbtrip.support.fixture;

import java.util.Locale;

import org.springframework.test.util.ReflectionTestUtils;

import live.lbtrip.domain.propensity.dto.LocalizedTravelProfile;
import live.lbtrip.domain.propensity.model.TravelProfile;

public final class TravelProfileFixture {

    public static final String CODE = "LVERG";
    public static final String NICKNAME = "동네 체험 메이트";
    public static final String DESCRIPTION = "함께하는 사람들과 부담 없이 로컬의 삶을 체험하는 여행자예요.";
    public static final String NICKNAME_EN = "Neighborhood Experience Mate";
    public static final String DESCRIPTION_EN = "A traveler who casually experiences local life with companions.";
    public static final String IMAGE_KEY = "travel-profiles/lverg.png";

    public static final String UPDATED_CODE = "HPERI";

    private TravelProfileFixture() {
    }

    public static TravelProfile travelProfile() {
        return TravelProfile.create(CODE, NICKNAME, DESCRIPTION, IMAGE_KEY);
    }

    public static TravelProfile travelProfileWithEnglish() {
        TravelProfile profile = travelProfile();
        ReflectionTestUtils.setField(profile, "nicknameEn", NICKNAME_EN);
        ReflectionTestUtils.setField(profile, "descriptionEn", DESCRIPTION_EN);
        return profile;
    }

    public static LocalizedTravelProfile localizedTravelProfile() {
        return LocalizedTravelProfile.of(travelProfile(), Locale.KOREAN);
    }

    public static LocalizedTravelProfile localizedTravelProfileEnglish() {
        return LocalizedTravelProfile.of(travelProfileWithEnglish(), Locale.ENGLISH);
    }

    public static TravelProfile featured(String code, String nickname, int featuredOrder) {
        TravelProfile profile = TravelProfile.create(
            code, nickname, nickname + " 설명", "travel-profiles/" + code.toLowerCase() + ".png");
        ReflectionTestUtils.setField(profile, "featuredOrder", featuredOrder);
        return profile;
    }
}
