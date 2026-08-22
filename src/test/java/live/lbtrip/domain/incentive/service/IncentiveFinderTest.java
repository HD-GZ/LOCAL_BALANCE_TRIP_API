package live.lbtrip.domain.incentive.service;

import static org.assertj.core.api.Assertions.assertThat;
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

import live.lbtrip.domain.incentive.model.vo.LocalizedIncentive;
import live.lbtrip.domain.incentive.repository.IncentiveRepository;
import live.lbtrip.global.i18n.MessageResolver;
import live.lbtrip.support.fixture.AdminIncentiveRequestFixture;
import live.lbtrip.support.fixture.IncentiveFixture;
import live.lbtrip.support.fixture.RegionCandidateFixture;

@ExtendWith(MockitoExtension.class)
class IncentiveFinderTest {

    @Mock
    private IncentiveRepository incentiveRepository;

    @Mock
    private MessageResolver messageResolver;

    @InjectMocks
    private IncentiveFinder incentiveFinder;

    @Nested
    class 로케일별_조회 {

        @Test
        void 한국어_로케일이면_한글_제목과_설명으로_조회한다() {
            when(messageResolver.currentLocale()).thenReturn(Locale.KOREAN);
            when(incentiveRepository.findAllByRegion(RegionCandidateFixture.CANDIDATE_ID))
                .thenReturn(List.of(IncentiveFixture.incentive()));

            List<LocalizedIncentive> incentives = incentiveFinder.findAllByRegion(RegionCandidateFixture.CANDIDATE_ID);

            assertThat(incentives.get(0).title()).isEqualTo(AdminIncentiveRequestFixture.TITLE);
            assertThat(incentives.get(0).description()).isEqualTo(AdminIncentiveRequestFixture.DESCRIPTION);
        }

        @Test
        void 영어_로케일이면_영문_제목과_설명으로_조회한다() {
            when(messageResolver.currentLocale()).thenReturn(Locale.ENGLISH);
            when(incentiveRepository.findActiveByRegion(RegionCandidateFixture.CANDIDATE_ID, LocalDate.now()))
                .thenReturn(List.of(IncentiveFixture.incentive()));

            List<LocalizedIncentive> incentives =
                incentiveFinder.findActiveByRegion(RegionCandidateFixture.CANDIDATE_ID, LocalDate.now());

            assertThat(incentives.get(0).title()).isEqualTo(AdminIncentiveRequestFixture.TITLE_EN);
            assertThat(incentives.get(0).description()).isEqualTo(AdminIncentiveRequestFixture.DESCRIPTION_EN);
        }
    }

    @Nested
    class 지역_없음 {

        @Test
        void 지역_후보_ID가_없으면_빈_목록을_반환한다() {
            assertThat(incentiveFinder.findAllByRegion(null)).isEmpty();
            assertThat(incentiveFinder.findActiveByRegion(null, LocalDate.now())).isEmpty();
        }
    }
}
