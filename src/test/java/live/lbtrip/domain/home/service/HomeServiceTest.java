package live.lbtrip.domain.home.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import live.lbtrip.domain.home.dto.response.ProfileSummaryResponse;
import live.lbtrip.domain.home.dto.response.ProfileTypeListResponse;
import live.lbtrip.domain.home.model.PropensityFactor;
import live.lbtrip.domain.propensity.model.Preference;
import live.lbtrip.domain.propensity.model.Propensity;
import live.lbtrip.domain.propensity.model.TravelProfile;
import live.lbtrip.domain.propensity.model.ValueConsumption;
import live.lbtrip.domain.propensity.repository.TravelProfileRepository;
import live.lbtrip.domain.propensity.service.PropensityFinder;
import live.lbtrip.domain.propensity.service.TravelProfileFinder;
import live.lbtrip.global.storage.service.ImageStorage;
import live.lbtrip.support.fixture.TravelProfileFixture;

@ExtendWith(MockitoExtension.class)
class HomeServiceTest {

    @Mock private TravelProfileRepository travelProfileRepository;
    @Mock private ImageStorage imageStorage;
    @Mock private PropensityFinder propensityFinder;
    @Mock private TravelProfileFinder travelProfileFinder;
    @Mock private PropensityFactorSelector propensityFactorSelector;
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

    @Test
    void 진단_요약을_슬라이더_세_개와_함께_반환한다() {
        Long userId = 1L;
        Propensity propensity = mock(Propensity.class);
        Preference preference = Preference.of(4, 5, 4, 2, 4);
        ValueConsumption vc = ValueConsumption.of(2, 4, 5, 2, 4);
        TravelProfile profile = TravelProfileFixture.featured("LVEAI", "찐로컬 탐험가", 1);

        when(propensityFinder.findByUserId(userId)).thenReturn(propensity);
        when(propensity.getPreference()).thenReturn(preference);
        when(propensity.getValueConsumption()).thenReturn(vc);
        when(propensity.getUpdatedAt()).thenReturn(LocalDateTime.of(2026, 7, 20, 9, 0));
        when(travelProfileFinder.findByPreference(preference)).thenReturn(profile);
        when(imageStorage.publicUrl("travel-profiles/lveai.png")).thenReturn("https://img/lveai.png");
        when(propensityFactorSelector.selectThree()).thenReturn(List.of(
            PropensityFactor.LOCALITY, PropensityFactor.VITALITY, PropensityFactor.SOCIALITY));

        ProfileSummaryResponse response = homeService.getProfileSummary(userId);

        assertThat(response.type()).isEqualTo("찐로컬 탐험가 (LVEAI)");
        assertThat(response.diagnosedAt()).isEqualTo(LocalDate.of(2026, 7, 20));
        assertThat(response.sliders()).hasSize(3);
        assertThat(response.sliders().get(0).minLabel()).isEqualTo("핫플·유명 명소");
        assertThat(response.sliders().get(0).score()).isEqualTo(4);
    }
}
