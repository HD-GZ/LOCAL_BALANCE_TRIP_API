package live.lbtrip.domain.propensity.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import live.lbtrip.domain.propensity.dto.request.PropensityRequest;
import live.lbtrip.domain.propensity.dto.response.PropensityResponse;
import live.lbtrip.domain.propensity.model.Propensity;
import live.lbtrip.domain.propensity.repository.PropensityRepository;
import live.lbtrip.domain.user.model.User;
import live.lbtrip.domain.user.repository.UserRepository;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PropensityService {

	private final PropensityRepository propensityRepository;
	private final UserRepository userRepository;
	private final PropensityClassifier classifier;

	@Transactional
	public PropensityResponse setPropensity(Long userId, PropensityRequest request) {
		Propensity propensity = propensityRepository.findByUserId(userId)
			.map(existing -> {
				existing.updateScores(
					request.locality(),
					request.frugality(),
					request.flexibility(),
					request.experientiality(),
					request.vitality(),
					request.sociality()
				);
				return existing;
			})
			.orElseGet(() -> {
				User userRef = userRepository.getReferenceById(userId);
				return propensityRepository.save(Propensity.create(
					userRef,
					request.locality(),
					request.frugality(),
					request.flexibility(),
					request.experientiality(),
					request.vitality(),
					request.sociality()
				));
			});

		return toResponse(propensity);
	}

	@Transactional(readOnly = true)
	public PropensityResponse getPropensity(Long userId) {
		Propensity propensity = propensityRepository.findByUserId(userId)
			.orElseThrow(() -> BusinessException.of(ErrorCode.PROPENSITY_NOT_FOUND));
		return toResponse(propensity);
	}

	private PropensityResponse toResponse(Propensity propensity) {
		PropensityResponse.Result result = classifier.classify(
			propensity.getLocality(),
			propensity.getFrugality(),
			propensity.getFlexibility(),
			propensity.getExperientiality(),
			propensity.getVitality(),
			propensity.getSociality()
		);
		return new PropensityResponse(
			result,
			propensity.getLocality(),
			propensity.getFrugality(),
			propensity.getFlexibility(),
			propensity.getExperientiality(),
			propensity.getVitality(),
			propensity.getSociality()
		);
	}
}
