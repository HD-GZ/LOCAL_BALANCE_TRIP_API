package live.lbtrip.domain.admin.incentive.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import live.lbtrip.domain.admin.incentive.dto.response.IncentiveResponse;
import live.lbtrip.domain.admin.incentive.model.Incentive;
import live.lbtrip.domain.admin.incentive.repository.IncentiveRepository;
import live.lbtrip.domain.region.repository.RegionCandidateRepository;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import live.lbtrip.support.fixture.IncentiveFixture;
import live.lbtrip.support.fixture.IncentiveRequestFixture;
import live.lbtrip.support.fixture.IncentiveResponseFixture;

@ExtendWith(MockitoExtension.class)
class IncentiveServiceTest {

    @Mock
    private IncentiveRepository incentiveRepository;

    @Mock
    private RegionCandidateRepository regionCandidateRepository;

    @InjectMocks
    private IncentiveService incentiveService;

    @Nested
    class 등록 {

        @Test
        void 인센티브를_등록한다() {
            Incentive incentive = IncentiveFixture.incentive();
            when(incentiveRepository.save(any(Incentive.class))).thenReturn(incentive);
            when(regionCandidateRepository.existsByLdongRegnCdAndLdongSignguCd(
                IncentiveRequestFixture.LDONG_REGN_CD, IncentiveRequestFixture.LDONG_SIGNGU_CD))
                .thenReturn(true);

            IncentiveResponse response = incentiveService.createIncentive(
                IncentiveRequestFixture.incentiveRequest()
            );

            assertThat(response.title()).isEqualTo(IncentiveRequestFixture.TITLE);
            assertThat(response.url()).isEqualTo(IncentiveRequestFixture.URL);
        }
    }

    @Nested
    class 목록_조회 {

        @Test
        void 인센티브_전체_목록을_조회한다() {
            when(incentiveRepository.findAll()).thenReturn(List.of(IncentiveFixture.incentive()));

            List<IncentiveResponse> responses = incentiveService.getIncentives();

            assertThat(responses).hasSize(1);
            assertThat(responses.get(0).title()).isEqualTo(IncentiveRequestFixture.TITLE);
            assertThat(responses.get(0).url()).isEqualTo(IncentiveRequestFixture.URL);
        }
    }

    @Nested
    class 수정 {

        @Test
        void 인센티브를_수정한다() {
            Incentive incentive = IncentiveFixture.incentive();
            when(incentiveRepository.findById(IncentiveResponseFixture.INCENTIVE_ID))
                .thenReturn(Optional.of(incentive));
            when(regionCandidateRepository.existsByLdongRegnCdAndLdongSignguCd(
                IncentiveRequestFixture.LDONG_REGN_CD, IncentiveRequestFixture.LDONG_SIGNGU_CD))
                .thenReturn(true);

            IncentiveResponse response = incentiveService.updateIncentive(
                IncentiveResponseFixture.INCENTIVE_ID,
                IncentiveRequestFixture.updatedIncentiveRequest()
            );

            assertThat(response.title()).isEqualTo(IncentiveRequestFixture.UPDATED_TITLE);
            assertThat(response.url()).isEqualTo(IncentiveRequestFixture.UPDATED_URL);
        }

        @Test
        void 인센티브가_존재하지_않으면_예외를_던진다() {
            when(incentiveRepository.findById(IncentiveResponseFixture.INCENTIVE_ID))
                .thenReturn(Optional.empty());

            assertThatThrownBy(() -> incentiveService.updateIncentive(
                IncentiveResponseFixture.INCENTIVE_ID,
                IncentiveRequestFixture.updatedIncentiveRequest()
            ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INCENTIVE_NOT_FOUND);
        }
    }

    @Nested
    class 삭제 {

        @Test
        void 인센티브를_삭제한다() {
            Incentive incentive = IncentiveFixture.incentive();
            when(incentiveRepository.findById(IncentiveResponseFixture.INCENTIVE_ID))
                .thenReturn(Optional.of(incentive));

            incentiveService.deleteIncentive(IncentiveResponseFixture.INCENTIVE_ID);

            verify(incentiveRepository).delete(incentive);
        }

        @Test
        void 인센티브가_존재하지_않으면_예외를_던진다() {
            when(incentiveRepository.findById(IncentiveResponseFixture.INCENTIVE_ID))
                .thenReturn(Optional.empty());

            assertThatThrownBy(() -> incentiveService.deleteIncentive(IncentiveResponseFixture.INCENTIVE_ID))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INCENTIVE_NOT_FOUND);
        }
    }
}
