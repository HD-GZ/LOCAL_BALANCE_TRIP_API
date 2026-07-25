package live.lbtrip.domain.recommendation.service;

import java.util.List;

import org.springframework.stereotype.Component;

import live.lbtrip.domain.recommendation.model.entity.RecommendedRegion;
import live.lbtrip.domain.recommendation.repository.RecommendedRegionRepository;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RecommendedRegionFinder {

    private final RecommendedRegionRepository recommendedRegionRepository;

    public List<RecommendedRegion> findAllByUserId(Long userId) {
        return recommendedRegionRepository.findAllByUserIdOrderByDisplayOrder(userId);
    }

    public RecommendedRegion findByIdAndUserId(Long id, Long userId) {
        return recommendedRegionRepository.findByIdAndUserId(id, userId)
            .orElseThrow(() -> BusinessException.of(ErrorCode.REGION_NOT_FOUND));
    }
}
