package live.lbtrip.domain.propensity.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import live.lbtrip.domain.propensity.dto.request.PropensityRequest;
import live.lbtrip.domain.propensity.dto.response.PropensityResponse;
import live.lbtrip.domain.propensity.model.Preference;
import live.lbtrip.domain.propensity.model.Propensity;
import live.lbtrip.domain.propensity.model.TravelProfile;
import live.lbtrip.domain.propensity.model.TravelProfileCode;
import live.lbtrip.domain.propensity.model.ValueConsumption;
import live.lbtrip.domain.propensity.repository.PropensityRepository;
import live.lbtrip.domain.propensity.repository.TravelProfileRepository;
import live.lbtrip.domain.user.model.User;
import live.lbtrip.domain.user.repository.UserRepository;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import live.lbtrip.global.storage.service.ImageStorage;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PropensityService {

    private final PropensityRepository propensityRepository;
    private final TravelProfileRepository travelProfileRepository;
    private final UserRepository userRepository;
    private final ImageStorage imageStorage;

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

        TravelProfile travelProfile = findTravelProfile(preference);
        return PropensityResponse.of(propensity, travelProfile, imageUrl(travelProfile));
    }

    public PropensityResponse getPropensity(Long userId) {
        Propensity propensity = propensityRepository.findByUserId(userId)
            .orElseThrow(() -> BusinessException.of(ErrorCode.PROPENSITY_NOT_FOUND));

        TravelProfile travelProfile = findTravelProfile(propensity.getPreference());
        return PropensityResponse.of(propensity, travelProfile, imageUrl(travelProfile));
    }

    private TravelProfile findTravelProfile(Preference preference) {
        return travelProfileRepository.findByCode(TravelProfileCode.from(preference).value())
            .orElseThrow(() -> BusinessException.of(ErrorCode.TRAVEL_PROFILE_NOT_FOUND));
    }

    private String imageUrl(TravelProfile travelProfile) {
        if (travelProfile.getImageKey() == null) {
            return null;
        }
        return imageStorage.publicUrl(travelProfile.getImageKey());
    }
}
