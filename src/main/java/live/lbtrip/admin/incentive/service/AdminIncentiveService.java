package live.lbtrip.admin.incentive.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import live.lbtrip.admin.incentive.dto.request.AdminIncentiveRequest;
import live.lbtrip.admin.incentive.dto.response.AdminIncentiveResponse;
import live.lbtrip.admin.incentive.repository.AdminIncentiveRepository;
import live.lbtrip.domain.incentive.model.Incentive;
import live.lbtrip.domain.incentive.model.IncentiveRegion;
import live.lbtrip.domain.region.repository.RegionCandidateRepository;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import live.lbtrip.global.util.StringNormalizer;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminIncentiveService {

    private final AdminIncentiveRepository adminIncentiveRepository;
    private final RegionCandidateRepository regionCandidateRepository;

    @Transactional
    public AdminIncentiveResponse createIncentive(AdminIncentiveRequest request) {
        List<AdminIncentiveRequest.RegionRequest> regions = validateRegions(request.regions());

        Incentive incentive = Incentive.create(
            StringNormalizer.trim(request.title()),
            StringNormalizer.trim(request.url()),
            normalizeDescription(request.description())
        );
        incentive.replaceRegions(toIncentiveRegions(regions));
        return AdminIncentiveResponse.from(adminIncentiveRepository.save(incentive));
    }

    public List<AdminIncentiveResponse> getIncentives() {
        return adminIncentiveRepository.findAll().stream()
            .map(AdminIncentiveResponse::from)
            .toList();
    }

    @Transactional
    public AdminIncentiveResponse updateIncentive(Long incentiveId, AdminIncentiveRequest request) {
        Incentive incentive = findIncentive(incentiveId);
        List<AdminIncentiveRequest.RegionRequest> regions = validateRegions(request.regions());

        incentive.update(
            StringNormalizer.trim(request.title()),
            StringNormalizer.trim(request.url()),
            normalizeDescription(request.description())
        );
        incentive.replaceRegions(toIncentiveRegions(regions));
        return AdminIncentiveResponse.from(incentive);
    }

    @Transactional
    public void deleteIncentive(Long incentiveId) {
        Incentive incentive = findIncentive(incentiveId);
        adminIncentiveRepository.delete(incentive);
    }

    private Incentive findIncentive(Long incentiveId) {
        return adminIncentiveRepository.findById(incentiveId)
            .orElseThrow(() -> BusinessException.of(ErrorCode.INCENTIVE_NOT_FOUND));
    }

    private List<AdminIncentiveRequest.RegionRequest> validateRegions(
        List<AdminIncentiveRequest.RegionRequest> regions
    ) {
        List<AdminIncentiveRequest.RegionRequest> distinctRegions = regions.stream().distinct().toList();
        for (AdminIncentiveRequest.RegionRequest region : distinctRegions) {
            if (!regionCandidateRepository.existsByLdongRegnCdAndLdongSignguCd(
                    region.ldongRegnCd(), region.ldongSignguCd())) {
                throw BusinessException.of(ErrorCode.INCENTIVE_REGION_INVALID);
            }
        }
        return distinctRegions;
    }

    private List<IncentiveRegion> toIncentiveRegions(List<AdminIncentiveRequest.RegionRequest> regions) {
        return regions.stream()
            .map(region -> IncentiveRegion.create(region.ldongRegnCd(), region.ldongSignguCd()))
            .toList();
    }

    private String normalizeDescription(String description) {
        return description == null ? null : StringNormalizer.trim(description);
    }
}
