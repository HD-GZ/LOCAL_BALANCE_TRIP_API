package live.lbtrip.domain.home.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import live.lbtrip.domain.home.dto.response.HeroResponse;
import live.lbtrip.domain.home.dto.response.ProfileSummaryResponse;
import live.lbtrip.domain.home.dto.response.ProfileTypeListResponse;
import live.lbtrip.domain.propensity.model.Preference;
import live.lbtrip.domain.propensity.model.Propensity;
import live.lbtrip.domain.propensity.model.TravelProfile;
import live.lbtrip.domain.propensity.model.ValueConsumption;
import live.lbtrip.domain.propensity.repository.TravelProfileRepository;
import live.lbtrip.domain.propensity.service.PropensityFinder;
import live.lbtrip.domain.propensity.service.TravelProfileFinder;
import live.lbtrip.domain.recommendation.repository.RecommendedRegionRepository;
import live.lbtrip.domain.tourism.repository.TourPlaceRepository;
import live.lbtrip.global.storage.service.ImageStorage;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HomeService {

    public static final int HERO_SIZE = 5;

    private final TravelProfileRepository travelProfileRepository;
    private final ImageStorage imageStorage;
    private final PropensityFinder propensityFinder;
    private final TravelProfileFinder travelProfileFinder;
    private final PropensityFactorSelector propensityFactorSelector;
    private final TourPlaceRepository tourPlaceRepository;
    private final RecommendedRegionRepository recommendedRegionRepository;

    public HeroResponse getHero(Long userId) {
        List<HeroResponse.InnerHeroItem> items = (userId == null)
            ? tourPlaceRepository.findRandomWithImage(HERO_SIZE).stream()
                .map(p -> new HeroResponse.InnerHeroItem(p.getImageUrl(), p.getTitle()))
                .toList()
            : recommendedRegionRepository.findAllByUserIdOrderByDisplayOrder(userId).stream()
                .map(r -> new HeroResponse.InnerHeroItem(r.getImageUrl(), r.getRegionName()))
                .toList();
        return HeroResponse.of(items);
    }

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
