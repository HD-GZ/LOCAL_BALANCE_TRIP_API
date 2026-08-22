package live.lbtrip.domain.propensity.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import live.lbtrip.domain.propensity.dto.LocalizedTravelProfile;
import live.lbtrip.domain.propensity.dto.request.PropensityRequest;
import live.lbtrip.domain.propensity.dto.response.PropensityResponse;
import live.lbtrip.domain.propensity.model.Preference;
import live.lbtrip.domain.propensity.model.Propensity;
import live.lbtrip.domain.propensity.model.ValueConsumption;
import live.lbtrip.domain.propensity.repository.PropensityRepository;
import live.lbtrip.domain.user.model.User;
import live.lbtrip.domain.user.service.UserFinder;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import live.lbtrip.global.storage.service.ImageStorage;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PropensityService {

    private final PropensityRepository propensityRepository;
    private final TravelProfileFinder travelProfileFinder;
    private final UserFinder userFinder;
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
                User user = userFinder.findById(userId);
                return propensityRepository.save(Propensity.create(user, preference, valueConsumption));
            });

        LocalizedTravelProfile travelProfile = travelProfileFinder.findLocalizedByPreference(preference);
        return PropensityResponse.of(propensity, travelProfile, imageStorage.publicUrl(travelProfile.imageKey()));
    }

    public PropensityResponse getPropensity(Long userId) {
        Propensity propensity = propensityRepository.findByUserId(userId)
            .orElseThrow(() -> BusinessException.of(ErrorCode.PROPENSITY_NOT_FOUND));

        LocalizedTravelProfile travelProfile = travelProfileFinder.findLocalizedByPreference(propensity.getPreference());
        return PropensityResponse.of(propensity, travelProfile, imageStorage.publicUrl(travelProfile.imageKey()));
    }
}
