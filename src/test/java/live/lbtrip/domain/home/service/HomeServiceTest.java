package live.lbtrip.domain.home.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import live.lbtrip.domain.home.dto.response.ProfileTypeListResponse;
import live.lbtrip.domain.propensity.model.TravelProfile;
import live.lbtrip.domain.propensity.repository.TravelProfileRepository;
import live.lbtrip.global.storage.service.ImageStorage;
import live.lbtrip.support.fixture.TravelProfileFixture;

@ExtendWith(MockitoExtension.class)
class HomeServiceTest {

    @Mock private TravelProfileRepository travelProfileRepository;
    @Mock private ImageStorage imageStorage;
    @InjectMocks private HomeService homeService;

    @Test
    void 대표_유형을_featured_order_순으로_반환한다() {
        List<TravelProfile> profiles = List.of(
            TravelProfileFixture.featured("LVEAI", "찐로컬 탐험가", 1),
            TravelProfileFixture.featured("HVEAG", "미식 수집가", 2));
        when(travelProfileRepository.findByFeaturedOrderIsNotNullOrderByFeaturedOrderAsc())
            .thenReturn(profiles);
        when(imageStorage.publicUrl("travel-profiles/lveai.png")).thenReturn("https://img/lveai.png");
        when(imageStorage.publicUrl("travel-profiles/hveag.png")).thenReturn("https://img/hveag.png");

        ProfileTypeListResponse response = homeService.getProfileTypes();

        assertThat(response.types()).hasSize(2);
        assertThat(response.types().get(0).nickname()).isEqualTo("찐로컬 탐험가");
        assertThat(response.types().get(0).imageUrl()).isEqualTo("https://img/lveai.png");
    }
}
