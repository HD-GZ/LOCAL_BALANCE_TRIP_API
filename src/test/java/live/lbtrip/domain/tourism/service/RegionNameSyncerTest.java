package live.lbtrip.domain.tourism.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import live.lbtrip.domain.region.model.RegionCandidate;
import live.lbtrip.domain.region.repository.RegionCandidateRepository;
import live.lbtrip.domain.tourism.client.TourApiClient;
import live.lbtrip.domain.tourism.client.dto.RegionNameItem;

@ExtendWith(MockitoExtension.class)
class RegionNameSyncerTest {

    @Mock
    private TourApiClient tourApiClient;

    @Mock
    private RegionCandidateRepository regionCandidateRepository;

    @InjectMocks
    private RegionNameSyncer regionNameSyncer;

    @Test
    void 시도별로_한_번만_조회해_시군구_영문명을_지역_후보에_채운다() {
        RegionCandidate damyang = RegionCandidate.create("전라남도 담양군", "46", "710");
        RegionCandidate gokseong = RegionCandidate.create("전라남도 곡성군", "46", "720");
        RegionCandidate hongseong = RegionCandidate.create("충청남도 홍성군", "44", "150");
        when(regionCandidateRepository.findAll()).thenReturn(List.of(damyang, gokseong, hongseong));
        when(tourApiClient.fetchRegionNames(Locale.ENGLISH, "46")).thenReturn(List.of(
            new RegionNameItem("46", "710", "Jeollanam-do", "Damyang-gun"),
            new RegionNameItem("46", "720", "Jeollanam-do", "Gokseong-gun")));
        when(tourApiClient.fetchRegionNames(Locale.ENGLISH, "44")).thenReturn(List.of());

        regionNameSyncer.syncEnglishNames();

        verify(tourApiClient, times(1)).fetchRegionNames(Locale.ENGLISH, "46");
        verify(tourApiClient, times(1)).fetchRegionNames(Locale.ENGLISH, "44");
        assertThat(damyang.getNameEn()).isEqualTo("Damyang-gun, Jeollanam-do");
        assertThat(gokseong.getNameEn()).isEqualTo("Gokseong-gun, Jeollanam-do");
        assertThat(hongseong.getNameEn()).isNull();
        verify(regionCandidateRepository).save(damyang);
        verify(regionCandidateRepository).save(gokseong);
        verify(regionCandidateRepository, never()).save(hongseong);
    }
}
