package live.lbtrip.domain.propensity.model.vo;

import java.util.Locale;

import live.lbtrip.domain.propensity.model.TravelProfile;

public record LocalizedTravelProfile(
    String code,
    String nickname,
    String description,
    String imageKey
) {

    public static LocalizedTravelProfile of(TravelProfile profile, Locale locale) {
        return new LocalizedTravelProfile(
            profile.getCode(),
            profile.nicknameFor(locale),
            profile.descriptionFor(locale),
            profile.getImageKey());
    }
}
