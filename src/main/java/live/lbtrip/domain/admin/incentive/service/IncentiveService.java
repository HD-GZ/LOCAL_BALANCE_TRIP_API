package live.lbtrip.domain.admin.incentive.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import live.lbtrip.domain.admin.incentive.dto.request.IncentiveRequest;
import live.lbtrip.domain.admin.incentive.dto.response.IncentiveResponse;
import live.lbtrip.domain.admin.incentive.model.Incentive;
import live.lbtrip.domain.admin.incentive.model.IncentiveRegion;
import live.lbtrip.domain.admin.incentive.repository.IncentiveRepository;
import live.lbtrip.domain.recommendation.repository.RegionCandidateRepository;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import live.lbtrip.global.util.StringNormalizer;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IncentiveService {

    private final IncentiveRepository incentiveRepository;
    private final RegionCandidateRepository regionCandidateRepository;

    @Transactional
    public IncentiveResponse createIncentive(IncentiveRequest request) {
        List<IncentiveRequest.RegionRequest> regions = validateRegions(request.regions());

        Incentive incentive = Incentive.create(
            StringNormalizer.trim(request.title()),
            StringNormalizer.trim(request.url()),
            normalizeDescription(request.description())
        );
        incentive.replaceRegions(toIncentiveRegions(regions));
        return IncentiveResponse.from(incentiveRepository.save(incentive));
    }

    public List<IncentiveResponse> getIncentives() {
        return incentiveRepository.findAll().stream()
            .map(IncentiveResponse::from)
            .toList();
    }

    @Transactional
    public IncentiveResponse updateIncentive(Long incentiveId, IncentiveRequest request) {
        Incentive incentive = findIncentive(incentiveId);
        List<IncentiveRequest.RegionRequest> regions = validateRegions(request.regions());

        incentive.update(
            StringNormalizer.trim(request.title()),
            StringNormalizer.trim(request.url()),
            normalizeDescription(request.description())
        );
        incentive.replaceRegions(toIncentiveRegions(regions));
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

    private List<IncentiveRequest.RegionRequest> validateRegions(List<IncentiveRequest.RegionRequest> regions) {
        List<IncentiveRequest.RegionRequest> distinctRegions = regions.stream().distinct().toList();
        for (IncentiveRequest.RegionRequest region : distinctRegions) {
            if (!regionCandidateRepository.existsByLdongRegnCdAndLdongSignguCd(
                    region.ldongRegnCd(), region.ldongSignguCd())) {
                throw BusinessException.of(ErrorCode.INCENTIVE_REGION_INVALID);
            }
        }
        return distinctRegions;
    }

    private List<IncentiveRegion> toIncentiveRegions(List<IncentiveRequest.RegionRequest> regions) {
        return regions.stream()
            .map(region -> IncentiveRegion.create(region.ldongRegnCd(), region.ldongSignguCd()))
            .toList();
    }

    private String normalizeDescription(String description) {
        return description == null ? null : StringNormalizer.trim(description);
    }
}
