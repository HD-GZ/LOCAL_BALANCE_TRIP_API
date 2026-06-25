package live.lbtrip.domain.propensity.service;

import org.springframework.stereotype.Service;

import live.lbtrip.domain.propensity.dto.request.PropensityRequest;
import live.lbtrip.domain.propensity.dto.response.PropensityResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PropensityService {

	public PropensityResponse setPropensity(Long userId, PropensityRequest request) {
		return null;
	}

	public PropensityResponse getPropensity(Long userId) {
		return null;
	}
}
