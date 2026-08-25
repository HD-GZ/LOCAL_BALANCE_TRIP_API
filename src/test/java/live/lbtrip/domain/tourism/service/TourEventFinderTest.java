package live.lbtrip.domain.tourism.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import live.lbtrip.domain.tourism.model.vo.LocalizedTourEvent;
import live.lbtrip.domain.tourism.repository.TourEventRepository;
import live.lbtrip.global.i18n.MessageResolver;
import live.lbtrip.support.fixture.RegionCandidateFixture;
import live.lbtrip.support.fixture.TourEventFixture;

@ExtendWith(MockitoExtension.class)
class TourEventFinderTest {

    @Mock
    private TourEventRepository tourEventRepository;

    @Mock
    private MessageResolver messageResolver;

    @InjectMocks
    private TourEventFinder tourEventFinder;

    @Nested
    class 진행중_행사_조회 {

        @Test
        void 현재_로케일의_종료되지_않은_행사를_제한_개수만큼_VO로_반환한다() {
            LocalDate today = LocalDate.of(2026, 8, 25);
            when(messageResolver.currentLocale()).thenReturn(Locale.ENGLISH);
            when(tourEventRepository.findActiveByRegion(
                Locale.ENGLISH, RegionCandidateFixture.CANDIDATE_ID, today, PageRequest.of(0, TourEventFinder.LIMIT)))
                .thenReturn(List.of(TourEventFixture.event("1", today, today.plusDays(2))));

            List<LocalizedTourEvent> events = tourEventFinder.findActiveByRegion(RegionCandidateFixture.CANDIDATE_ID, today);

            assertThat(TourEventFinder.LIMIT).isEqualTo(5);
            assertThat(events).singleElement().satisfies(event -> {
                assertThat(event.title()).isEqualTo(TourEventFixture.TITLE);
                assertThat(event.imageUrl()).isEqualTo(TourEventFixture.IMAGE_URL);
                assertThat(event.startDate()).isEqualTo(today);
                assertThat(event.endDate()).isEqualTo(today.plusDays(2));
                assertThat(event.address()).isEqualTo(TourEventFixture.ADDRESS);
            });
        }

        @Test
        void 지역_ID가_없으면_빈_목록을_반환한다() {
            assertThat(tourEventFinder.findActiveByRegion(null, LocalDate.now())).isEmpty();
            verifyNoInteractions(tourEventRepository);
        }
    }
}
