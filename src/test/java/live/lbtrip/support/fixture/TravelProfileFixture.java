package live.lbtrip.support.fixture;

import live.lbtrip.domain.propensity.model.TravelProfile;

public final class TravelProfileFixture {

    public static final String CODE = "LVERG";
    public static final String NICKNAME = "동네 체험 메이트";
    public static final String DESCRIPTION = "함께하는 사람들과 부담 없이 로컬의 삶을 체험하는 여행자예요.";
    public static final String IMAGE_KEY = "travel-profiles/lverg.png";

    public static final String UPDATED_CODE = "HPERI";

    private TravelProfileFixture() {
    }

    public static TravelProfile travelProfile() {
        return TravelProfile.create(CODE, NICKNAME, DESCRIPTION, IMAGE_KEY);
    }
}
