package live.lbtrip.domain.propensity.service;

import org.springframework.stereotype.Component;

import live.lbtrip.domain.propensity.model.Propensity;
import live.lbtrip.domain.propensity.repository.PropensityRepository;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PropensityFinder {

    private final PropensityRepository propensityRepository;

    public Propensity findByUserId(Long userId) {
        return propensityRepository.findByUserId(userId)
            .orElseThrow(() -> BusinessException.of(ErrorCode.PROPENSITY_NOT_FOUND));
    }
}
