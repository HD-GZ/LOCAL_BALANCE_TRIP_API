package live.lbtrip.domain.propensity.service;

import org.springframework.stereotype.Component;

import live.lbtrip.domain.propensity.model.Preference;
import live.lbtrip.domain.propensity.model.TravelProfile;
import live.lbtrip.domain.propensity.repository.TravelProfileRepository;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TravelProfileFinder {

    private final TravelProfileRepository travelProfileRepository;

    public TravelProfile findByPreference(Preference preference) {
        return travelProfileRepository.findByCode(preference.toTravelProfileCode())
            .orElseThrow(() -> BusinessException.of(ErrorCode.TRAVEL_PROFILE_NOT_FOUND));
    }
}
