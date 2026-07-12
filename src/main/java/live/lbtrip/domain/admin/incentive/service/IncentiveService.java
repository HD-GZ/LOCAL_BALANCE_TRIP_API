package live.lbtrip.domain.admin.incentive.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import live.lbtrip.domain.admin.incentive.dto.request.IncentiveRequest;
import live.lbtrip.domain.admin.incentive.dto.response.IncentiveResponse;
import live.lbtrip.domain.admin.incentive.model.Incentive;
import live.lbtrip.domain.admin.incentive.repository.IncentiveRepository;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import live.lbtrip.global.util.StringNormalizer;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IncentiveService {

    private final IncentiveRepository incentiveRepository;

    @Transactional
    public IncentiveResponse createIncentive(IncentiveRequest request) {
        Incentive incentive = incentiveRepository.save(Incentive.create(
            StringNormalizer.trim(request.title()),
            StringNormalizer.trim(request.url())
        ));
        return IncentiveResponse.from(incentive);
    }

    public List<IncentiveResponse> getIncentives() {
        return incentiveRepository.findAll().stream()
            .map(IncentiveResponse::from)
            .toList();
    }

    @Transactional
    public IncentiveResponse updateIncentive(Long incentiveId, IncentiveRequest request) {
        Incentive incentive = findIncentive(incentiveId);
        incentive.update(
            StringNormalizer.trim(request.title()),
            StringNormalizer.trim(request.url())
        );
        return IncentiveResponse.from(incentive);
    }

    @Transactional
    public void deleteIncentive(Long incentiveId) {
        Incentive incentive = findIncentive(incentiveId);
        incentiveRepository.delete(incentive);
    }

    private Incentive findIncentive(Long incentiveId) {
        return incentiveRepository.findById(incentiveId)
            .orElseThrow(() -> BusinessException.of(ErrorCode.INCENTIVE_NOT_FOUND));
    }
}
