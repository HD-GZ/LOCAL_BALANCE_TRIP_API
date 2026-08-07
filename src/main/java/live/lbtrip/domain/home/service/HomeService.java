package live.lbtrip.domain.home.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import live.lbtrip.domain.home.dto.response.ProfileSummaryResponse;
import live.lbtrip.domain.home.dto.response.ProfileTypeListResponse;
import live.lbtrip.domain.propensity.model.Preference;
import live.lbtrip.domain.propensity.model.Propensity;
import live.lbtrip.domain.propensity.model.TravelProfile;
import live.lbtrip.domain.propensity.model.ValueConsumption;
import live.lbtrip.domain.propensity.repository.TravelProfileRepository;
import live.lbtrip.domain.propensity.service.PropensityFinder;
import live.lbtrip.domain.propensity.service.TravelProfileFinder;
import live.lbtrip.global.storage.service.ImageStorage;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HomeService {

    private final TravelProfileRepository travelProfileRepository;
    private final ImageStorage imageStorage;
    private final PropensityFinder propensityFinder;
    private final TravelProfileFinder travelProfileFinder;
    private final PropensityFactorSelector propensityFactorSelector;

    public ProfileTypeListResponse getProfileTypes() {
        return ProfileTypeListResponse.of(
            travelProfileRepository.findByFeaturedOrderIsNotNullOrderByFeaturedOrderAsc(),
            imageStorage);
    }

    public ProfileSummaryResponse getProfileSummary(Long userId) {
        Propensity propensity = propensityFinder.findByUserId(userId);
        Preference preference = propensity.getPreference();
        ValueConsumption valueConsumption = propensity.getValueConsumption();
        TravelProfile profile = travelProfileFinder.findByPreference(preference);

        return ProfileSummaryResponse.of(
            profile,
            imageStorage.publicUrl(profile.getImageKey()),
            propensity.getUpdatedAt().toLocalDate(),
            preference,
            valueConsumption,
            propensityFactorSelector.selectThree());
    }
}
