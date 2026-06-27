package live.lbtrip.domain.propensity.service;

import org.springframework.stereotype.Component;

import live.lbtrip.domain.propensity.dto.response.PropensityResponse;
import live.lbtrip.domain.propensity.model.PropensityScores;

@Component
public class PropensityClassifier {

    // TODO: src/main/resources/prompt/local-balance-trip-result-prompt-en.md 기반 LLM 호출로 교체
    public PropensityResponse.Result classify(PropensityScores scores) {
        return new PropensityResponse.Result(
            "실속형 로컬 감성 여행자",
            "럭셔리보다 실속을, 유명 명소보다 골목 상권을, 빡빡한 일정보다 감성 여백을 즐기는 1인 여행자예요."
        );
    }
}
