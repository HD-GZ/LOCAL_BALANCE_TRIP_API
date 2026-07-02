package live.lbtrip.domain.propensity.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import live.lbtrip.domain.propensity.dto.request.PropensityRequest;
import live.lbtrip.domain.propensity.dto.response.PropensityResponse;
import live.lbtrip.domain.propensity.model.Preference;
import live.lbtrip.domain.propensity.model.Propensity;
import live.lbtrip.domain.propensity.model.PropensityBucket;
import live.lbtrip.domain.propensity.model.TravelProfile;
import live.lbtrip.domain.propensity.model.ValueConsumption;
import live.lbtrip.domain.propensity.repository.PropensityRepository;
import live.lbtrip.domain.propensity.repository.TravelProfileRepository;
import live.lbtrip.domain.user.model.User;
import live.lbtrip.domain.user.repository.UserRepository;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PropensityService {

    private final PropensityRepository propensityRepository;
    private final TravelProfileRepository travelProfileRepository;
    private final UserRepository userRepository;

    @Transactional
    public PropensityResponse setPropensity(Long userId, PropensityRequest request) {
        Preference preference = request.toPreference();
        ValueConsumption valueConsumption = request.toValueConsumption();

        Propensity propensity = propensityRepository.findByUserId(userId)
            .map(existing -> {
                existing.update(preference, valueConsumption);
                return existing;
            })
            .orElseGet(() -> {
                User userRef = userRepository.getReferenceById(userId);
                return propensityRepository.save(Propensity.create(userRef, preference, valueConsumption));
            });

        TravelProfile travelProfile = findTravelProfile(preference, valueConsumption);
        return PropensityResponse.of(propensity, travelProfile);
    }

    public PropensityResponse getPropensity(Long userId) {
        Propensity propensity = propensityRepository.findByUserId(userId)
            .orElseThrow(() -> BusinessException.of(ErrorCode.PROPENSITY_NOT_FOUND));

        TravelProfile travelProfile = findTravelProfile(
            propensity.getPreference(),
            propensity.getValueConsumption()
        );

        return PropensityResponse.of(propensity, travelProfile);
    }

    private TravelProfile findTravelProfile(Preference preference, ValueConsumption valueConsumption) {
        return travelProfileRepository.findByBuckets(
            PropensityBucket.fromScore(preference.getLocality()),
            PropensityBucket.fromScore(preference.getFrugality()),
            PropensityBucket.fromScore(preference.getExperientiality()),
            PropensityBucket.fromScore(preference.getVitality()),
            PropensityBucket.fromScore(preference.getSociality()),
            PropensityBucket.fromScore(valueConsumption.getAccommodation()),
            PropensityBucket.fromScore(valueConsumption.getFood()),
            PropensityBucket.fromScore(valueConsumption.getExperience()),
            PropensityBucket.fromScore(valueConsumption.getTransportation()),
            PropensityBucket.fromScore(valueConsumption.getCafeExhibition())
        ).orElseThrow(() -> BusinessException.of(ErrorCode.TRAVEL_PROFILE_NOT_FOUND));
    }
}
