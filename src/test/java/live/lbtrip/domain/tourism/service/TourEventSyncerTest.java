package live.lbtrip.domain.tourism.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import live.lbtrip.domain.region.model.RegionCandidate;
import live.lbtrip.domain.tourism.client.TourApiClient;
import live.lbtrip.domain.tourism.model.entity.TourEvent;
import live.lbtrip.domain.tourism.repository.TourEventRepository;
import live.lbtrip.support.fixture.RegionCandidateFixture;
import live.lbtrip.support.fixture.TourEventFixture;

@ExtendWith(MockitoExtension.class)
class TourEventSyncerTest {

    private static final LocalDate TODAY = LocalDate.now();

    @Mock
    private TourApiClient tourApiClient;

    @Mock
    private TourEventRepository tourEventRepository;

    @InjectMocks
    private TourEventSyncer tourEventSyncer;

    @Nested
    class 행사_적재 {

        @Test
        void 오늘부터_60일_구간을_조회하고_새_행사는_저장한다() {
            RegionCandidate candidate = RegionCandidateFixture.candidateWithId();
            when(tourApiClient.fetchFestivals(
                candidate.getLdongRegnCd(), candidate.getLdongSignguCd(),
                TODAY, TODAY.plusDays(TourEventSyncer.WINDOW_DAYS), Locale.KOREAN))
                .thenReturn(List.of(TourEventFixture.item("1", TODAY.plusDays(1), TODAY.plusDays(3))));
            when(tourEventRepository.findByLocaleAndContentId(Locale.KOREAN, "1")).thenReturn(Optional.empty());
            when(tourEventRepository.findAllByLocaleAndRegionCandidateId(Locale.KOREAN, candidate.getId()))
                .thenReturn(List.of());

            tourEventSyncer.sync(candidate, Locale.KOREAN);

            assertThat(TourEventSyncer.WINDOW_DAYS).isEqualTo(60);
            ArgumentCaptor<TourEvent> captor = ArgumentCaptor.forClass(TourEvent.class);
            verify(tourEventRepository).save(captor.capture());
            assertThat(captor.getValue().getLocale()).isEqualTo(Locale.KOREAN);
            assertThat(captor.getValue().getContentId()).isEqualTo("1");
            assertThat(captor.getValue().getRegionCandidate()).isSameAs(candidate);
            verify(tourEventRepository, never()).deleteAll(any());
        }

        @Test
        void 이미_있는_행사는_갱신하고_응답에_없거나_종료된_행사는_삭제한다() {
            RegionCandidate candidate = RegionCandidateFixture.candidateWithId();
            TourEvent existing = TourEventFixture.event(candidate, Locale.KOREAN, "1", TODAY, TODAY.plusDays(1));
            TourEvent missing = TourEventFixture.event(candidate, Locale.KOREAN, "2", TODAY, TODAY.plusDays(1));
            TourEvent ended = TourEventFixture.event(candidate, Locale.KOREAN, "3", TODAY.minusDays(5), TODAY.minusDays(1));
            when(tourApiClient.fetchFestivals(any(), any(), any(), any(), eq(Locale.KOREAN)))
                .thenReturn(List.of(
                    TourEventFixture.item("1", TODAY.plusDays(2), TODAY.plusDays(4)),
                    TourEventFixture.item("3", TODAY.minusDays(5), TODAY.minusDays(1))));
            when(tourEventRepository.findByLocaleAndContentId(Locale.KOREAN, "1")).thenReturn(Optional.of(existing));
            when(tourEventRepository.findAllByLocaleAndRegionCandidateId(Locale.KOREAN, candidate.getId()))
                .thenReturn(List.of(existing, missing, ended));

            tourEventSyncer.sync(candidate, Locale.KOREAN);

            assertThat(existing.getEventStart()).isEqualTo(TODAY.plusDays(2));
            verify(tourEventRepository).save(existing);
            ArgumentCaptor<List<TourEvent>> captor = ArgumentCaptor.captor();
            verify(tourEventRepository).deleteAll(captor.capture());
            assertThat(captor.getValue()).containsExactlyInAnyOrder(missing, ended);
        }
    }
}
