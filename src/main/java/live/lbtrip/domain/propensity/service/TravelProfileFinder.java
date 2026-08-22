package live.lbtrip.domain.propensity.service;

import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Component;

import live.lbtrip.domain.propensity.dto.LocalizedTravelProfile;
import live.lbtrip.domain.propensity.model.Preference;
import live.lbtrip.domain.propensity.model.TravelProfile;
import live.lbtrip.domain.propensity.repository.TravelProfileRepository;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import live.lbtrip.global.i18n.MessageResolver;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TravelProfileFinder {

    private final TravelProfileRepository travelProfileRepository;
    private final MessageResolver messageResolver;

    public TravelProfile findByPreference(Preference preference) {
        return travelProfileRepository.findByCode(preference.toTravelProfileCode())
            .orElseThrow(() -> BusinessException.of(ErrorCode.TRAVEL_PROFILE_NOT_FOUND));
    }

    public LocalizedTravelProfile findLocalizedByPreference(Preference preference) {
        return LocalizedTravelProfile.of(findByPreference(preference), messageResolver.currentLocale());
    }

    public List<LocalizedTravelProfile> findFeatured() {
        Locale locale = messageResolver.currentLocale();
        return travelProfileRepository.findByFeaturedOrderIsNotNullOrderByFeaturedOrderAsc().stream()
            .map(profile -> LocalizedTravelProfile.of(profile, locale))
            .toList();
    }
}
