package live.lbtrip.domain.home.dto.response;

import java.time.LocalDate;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

import live.lbtrip.domain.home.model.PropensityFactor;
import live.lbtrip.domain.propensity.model.Preference;
import live.lbtrip.domain.propensity.model.TravelProfile;
import live.lbtrip.domain.propensity.model.ValueConsumption;

public record ProfileSummaryResponse(
    @Schema(description = "진단 유형 라벨. \"{별칭} ({코드})\"", example = "찐로컬 탐험가 (LVEAI)")
    String type,

    @Schema(description = "유형 설명")
    String description,

    @Schema(description = "유형 캐릭터 이미지 URL")
    String imageUrl,

    @Schema(description = "진단 완료일", example = "2026-07-20")
    LocalDate diagnosedAt,

    @Schema(description = "취향 요소 슬라이더(랜덤 3개)")
    List<InnerSlider> sliders
) {

    public record InnerSlider(
        @Schema(description = "요소 키", example = "LOCALITY")
        String key,

        @Schema(description = "최소(1점) 라벨", example = "핫플·유명 명소")
        String minLabel,

        @Schema(description = "최대(5점) 라벨", example = "로컬·골목 상권")
        String maxLabel,

        @Schema(description = "현재 점수(1~5)", example = "4")
        int score
    ) {
    }

    public static ProfileSummaryResponse of(
        TravelProfile profile,
        String imageUrl,
        LocalDate diagnosedAt,
        Preference preference,
        ValueConsumption valueConsumption,
        List<PropensityFactor> factors
    ) {
        List<InnerSlider> sliders = factors.stream()
            .map(f -> new InnerSlider(
                f.name(),
                f.getMinLabel(),
                f.getMaxLabel(),
                f.score(preference, valueConsumption)))
            .toList();
        return new ProfileSummaryResponse(
            "%s (%s)".formatted(profile.getNickname(), profile.getCode()),
            profile.getDescription(),
            imageUrl,
            diagnosedAt,
            sliders);
    }
}
