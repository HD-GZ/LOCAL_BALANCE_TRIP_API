package live.lbtrip.domain.recommendation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import live.lbtrip.domain.recommendation.model.entity.RecommendedRegion;

public record RegionRecommendationResponse(
    @Schema(description = "추천 지역 식별자(서비스 생성 ID)", example = "1")
    Long regionId,

    @Schema(description = "지역 표시명(시/도 풀네임 + 시/군)", example = "전라남도 담양군")
    String regionName,

    @Schema(description = "대표 이미지 URL", example = "http://tong.visitkorea.or.kr/cms/resource/....jpg")
    String imageUrl,

    @Schema(description = "추천 이유", example = "골목 상권과 로컬 감성이 살아있는 소도시 여행지예요.")
    String reason
) {

    public static RegionRecommendationResponse from(RecommendedRegion region) {
        return new RegionRecommendationResponse(
            region.getId(),
            region.getRegionName(),
            region.getImageUrl(),
            region.getReason()
        );
    }
}
