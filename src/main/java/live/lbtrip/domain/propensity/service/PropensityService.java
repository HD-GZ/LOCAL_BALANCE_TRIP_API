package live.lbtrip.domain.propensity.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import live.lbtrip.domain.propensity.dto.request.PropensityRequest;
import live.lbtrip.domain.propensity.dto.response.PropensityResponse;
import live.lbtrip.domain.propensity.model.Propensity;
import live.lbtrip.domain.propensity.model.PropensityScores;
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
		PropensityScores scores = PropensityScores.from(request);

		Propensity propensity = propensityRepository.findByUserId(userId)
			.map(existing -> {
				existing.updateScores(scores);
				return existing;
			})
			.orElseGet(() -> {
				User userRef = userRepository.getReferenceById(userId);
				return propensityRepository.save(Propensity.builder()
					.user(userRef)
					.scores(scores)
					.build());
			});

		return PropensityResponse.of(propensity, classifier.classify(scores));
	}

	@Transactional(readOnly = true)
	public PropensityResponse getPropensity(Long userId) {
		Propensity propensity = propensityRepository.findByUserId(userId)
			.orElseThrow(() -> BusinessException.of(ErrorCode.PROPENSITY_NOT_FOUND));

		return PropensityResponse.of(
			propensity,
			classifier.classify(PropensityScores.from(propensity))
		);
	}
}
