package live.lbtrip.domain.propensity.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import live.lbtrip.domain.propensity.dto.request.PropensityRequest;
import live.lbtrip.domain.propensity.dto.response.PropensityResponse;
import live.lbtrip.domain.propensity.dto.response.PropensityResponse.InnerPropensityResultResponse;
import live.lbtrip.domain.propensity.model.Preference;
import live.lbtrip.domain.propensity.model.Propensity;
import live.lbtrip.domain.propensity.model.ValueConsumption;
import live.lbtrip.domain.propensity.repository.PropensityRepository;
import live.lbtrip.domain.user.model.User;
import live.lbtrip.domain.user.repository.UserRepository;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PropensityService {

    private final PropensityRepository propensityRepository;
    private final UserRepository userRepository;
    private final PropensityClassifier classifier;

    @Transactional
    public PropensityResponse setPropensity(Long userId, PropensityRequest request) {
        Preference preference = request.toPreference();
        ValueConsumption valueConsumption = request.toValueConsumption();

        Propensity propensity = propensityRepository.findByUserId(userId)
            .map(existing -> {
                existing.update(preference, valueConsumption);
                return existing;
            })
            .orElseGet(() -> {
                User userRef = userRepository.getReferenceById(userId);
                return propensityRepository.save(Propensity.create(userRef, preference, valueConsumption));
            });

        InnerPropensityResultResponse propensityResult = classifier.classify(preference, valueConsumption);
        return PropensityResponse.of(propensity, propensityResult.type(), propensityResult.description());
    }

    public PropensityResponse getPropensity(Long userId) {
        Propensity propensity = propensityRepository.findByUserId(userId)
            .orElseThrow(() -> BusinessException.of(ErrorCode.PROPENSITY_NOT_FOUND));

        InnerPropensityResultResponse propensityResult = classifier.classify(
            propensity.getPreference(),
            propensity.getValueConsumption()
        );

        return PropensityResponse.of(propensity, propensityResult.type(), propensityResult.description());
    }
}
