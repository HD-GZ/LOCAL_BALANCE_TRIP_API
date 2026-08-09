package live.lbtrip.admin.incentive.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
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

import live.lbtrip.admin.incentive.dto.request.AdminIncentiveRequest;
import live.lbtrip.admin.incentive.dto.response.AdminIncentiveResponse;
import live.lbtrip.admin.incentive.repository.AdminIncentiveRepository;
import live.lbtrip.domain.incentive.model.Incentive;
import live.lbtrip.domain.region.repository.RegionCandidateRepository;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import live.lbtrip.support.fixture.AdminIncentiveRequestFixture;
import live.lbtrip.support.fixture.AdminIncentiveResponseFixture;
import live.lbtrip.support.fixture.IncentiveFixture;

@ExtendWith(MockitoExtension.class)
class AdminIncentiveServiceTest {

    @Mock
    private AdminIncentiveRepository adminIncentiveRepository;

    @Mock
    private RegionCandidateRepository regionCandidateRepository;

    @InjectMocks
    private AdminIncentiveService adminIncentiveService;

    @Nested
    class 등록 {

        @Test
        void 인센티브를_등록한다() {
            Incentive incentive = IncentiveFixture.incentive();
            when(adminIncentiveRepository.save(any(Incentive.class))).thenReturn(incentive);
            when(regionCandidateRepository.existsByLdongRegnCdAndLdongSignguCd(
                AdminIncentiveRequestFixture.LDONG_REGN_CD,
                AdminIncentiveRequestFixture.LDONG_SIGNGU_CD))
                .thenReturn(true);

            AdminIncentiveResponse response = adminIncentiveService.createIncentive(
                AdminIncentiveRequestFixture.incentiveRequest()
            );

            assertThat(response.title()).isEqualTo(AdminIncentiveRequestFixture.TITLE);
            assertThat(response.url()).isEqualTo(AdminIncentiveRequestFixture.URL);
            assertThat(response.startDate()).isEqualTo(AdminIncentiveRequestFixture.START_DATE);
            assertThat(response.endDate()).isEqualTo(AdminIncentiveRequestFixture.END_DATE);
        }

        @Test
        void 종료일이_시작일보다_빠르면_예외를_던지고_인센티브를_저장하지_않는다() {
            when(regionCandidateRepository.existsByLdongRegnCdAndLdongSignguCd(
                AdminIncentiveRequestFixture.LDONG_REGN_CD,
                AdminIncentiveRequestFixture.LDONG_SIGNGU_CD))
                .thenReturn(true);

            assertThatThrownBy(() -> adminIncentiveService.createIncentive(new AdminIncentiveRequest(
                AdminIncentiveRequestFixture.TITLE,
                AdminIncentiveRequestFixture.URL,
                AdminIncentiveRequestFixture.DESCRIPTION,
                AdminIncentiveRequestFixture.START_DATE,
                AdminIncentiveRequestFixture.START_DATE.minusDays(1),
                AdminIncentiveRequestFixture.regions()
            )))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_INCENTIVE_PERIOD);

            verify(adminIncentiveRepository, never()).save(any(Incentive.class));
        }
    }

    @Nested
    class 목록_조회 {

        @Test
        void 인센티브_전체_목록을_조회한다() {
            when(adminIncentiveRepository.findAll()).thenReturn(List.of(IncentiveFixture.incentive()));

            List<AdminIncentiveResponse> responses = adminIncentiveService.getIncentives();

            assertThat(responses).hasSize(1);
            assertThat(responses.get(0).title()).isEqualTo(AdminIncentiveRequestFixture.TITLE);
            assertThat(responses.get(0).url()).isEqualTo(AdminIncentiveRequestFixture.URL);
        }
    }

    @Nested
    class 수정 {

        @Test
        void 인센티브를_수정한다() {
            Incentive incentive = IncentiveFixture.incentive();
            when(adminIncentiveRepository.findById(AdminIncentiveResponseFixture.INCENTIVE_ID))
                .thenReturn(Optional.of(incentive));
            when(regionCandidateRepository.existsByLdongRegnCdAndLdongSignguCd(
                AdminIncentiveRequestFixture.LDONG_REGN_CD,
                AdminIncentiveRequestFixture.LDONG_SIGNGU_CD))
                .thenReturn(true);

            AdminIncentiveResponse response = adminIncentiveService.updateIncentive(
                AdminIncentiveResponseFixture.INCENTIVE_ID,
                AdminIncentiveRequestFixture.updatedIncentiveRequest()
            );

            assertThat(response.title()).isEqualTo(AdminIncentiveRequestFixture.UPDATED_TITLE);
            assertThat(response.url()).isEqualTo(AdminIncentiveRequestFixture.UPDATED_URL);
            assertThat(response.startDate()).isEqualTo(AdminIncentiveRequestFixture.UPDATED_START_DATE);
            assertThat(response.endDate()).isNull();
        }

        @Test
        void 인센티브가_존재하지_않으면_예외를_던진다() {
            when(adminIncentiveRepository.findById(AdminIncentiveResponseFixture.INCENTIVE_ID))
                .thenReturn(Optional.empty());

            assertThatThrownBy(() -> adminIncentiveService.updateIncentive(
                AdminIncentiveResponseFixture.INCENTIVE_ID,
                AdminIncentiveRequestFixture.updatedIncentiveRequest()
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
            when(adminIncentiveRepository.findById(AdminIncentiveResponseFixture.INCENTIVE_ID))
                .thenReturn(Optional.of(incentive));

            adminIncentiveService.deleteIncentive(AdminIncentiveResponseFixture.INCENTIVE_ID);

            verify(adminIncentiveRepository).delete(incentive);
        }

        @Test
        void 인센티브가_존재하지_않으면_예외를_던진다() {
            when(adminIncentiveRepository.findById(AdminIncentiveResponseFixture.INCENTIVE_ID))
                .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                adminIncentiveService.deleteIncentive(AdminIncentiveResponseFixture.INCENTIVE_ID))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INCENTIVE_NOT_FOUND);
        }
    }
}
