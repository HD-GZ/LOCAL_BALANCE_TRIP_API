package live.lbtrip.domain.propensity.service;

import org.springframework.stereotype.Component;

import live.lbtrip.domain.propensity.dto.response.PropensityResponse;

@Component
public class PropensityClassifier {

	// TODO: 6축 점수 조합 → 진단 유형/설명 매핑 규칙 정의
	public PropensityResponse.Result classify(
		int locality,
		int frugality,
		int flexibility,
		int experientiality,
		int vitality,
		int sociality
	) {
		return new PropensityResponse.Result(
			"실속형 로컬 감성 여행자",
			"럭셔리보다 실속을, 유명 명소보다 골목 상권을, 빡빡한 일정보다 감성 여백을 즐기는 1인 여행자예요."
		);
	}
}
