package live.lbtrip.domain.home.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import live.lbtrip.domain.home.dto.response.ProfileTypeListResponse;
import live.lbtrip.domain.propensity.repository.TravelProfileRepository;
import live.lbtrip.global.storage.service.ImageStorage;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HomeService {

    private final TravelProfileRepository travelProfileRepository;
    private final ImageStorage imageStorage;

    public ProfileTypeListResponse getProfileTypes() {
        return ProfileTypeListResponse.of(
            travelProfileRepository.findByFeaturedOrderIsNotNullOrderByFeaturedOrderAsc(),
            imageStorage);
    }
}
