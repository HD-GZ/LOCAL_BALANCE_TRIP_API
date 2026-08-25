package live.lbtrip.domain.tourism.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.PageRequest;

import live.lbtrip.domain.region.model.RegionCandidate;
import live.lbtrip.domain.region.repository.RegionCandidateRepository;
import live.lbtrip.domain.tourism.model.entity.TourEvent;
import live.lbtrip.support.fixture.RegionCandidateFixture;
import live.lbtrip.support.fixture.TourEventFixture;

@DataJpaTest
class TourEventRepositoryTest {

    @Autowired
    private RegionCandidateRepository regionCandidateRepository;

    @Autowired
    private TourEventRepository tourEventRepository;

    @Test
    void 종료되지_않은_행사를_시작일_오름차순으로_제한_개수만큼_조회한다() {
        RegionCandidate candidate = regionCandidateRepository.save(RegionCandidateFixture.candidate());
        RegionCandidate other = regionCandidateRepository.save(RegionCandidate.create("전라남도 곡성군", "46", "720"));
        LocalDate today = LocalDate.of(2026, 8, 25);
        tourEventRepository.save(TourEventFixture.event(candidate, Locale.KOREAN, "ended", today.minusDays(10), today.minusDays(1)));
        tourEventRepository.save(TourEventFixture.event(candidate, Locale.KOREAN, "later", today.plusDays(20), today.plusDays(25)));
        tourEventRepository.save(TourEventFixture.event(candidate, Locale.KOREAN, "ongoing", today.minusDays(3), today));
        tourEventRepository.save(TourEventFixture.event(candidate, Locale.KOREAN, "soon", today.plusDays(1), today.plusDays(2)));
        tourEventRepository.save(TourEventFixture.event(candidate, Locale.ENGLISH, "english", today, today.plusDays(1)));
        tourEventRepository.save(TourEventFixture.event(other, Locale.KOREAN, "other", today, today.plusDays(1)));

        List<TourEvent> events = tourEventRepository.findActiveByRegion(
            Locale.KOREAN, candidate.getId(), today, PageRequest.of(0, 2));

        assertThat(events).extracting(TourEvent::getContentId).containsExactly("ongoing", "soon");
    }
}
