package live.lbtrip.domain.home.dto.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

public record HomeFeedResponse(
    @Schema(description = "저장 코스 + 추천 여행지 인터리빙 목록")
    List<InnerFeedItem> items
) {

    public record InnerFeedItem(
        @Schema(description = "아이템 타입", example = "SAVED_COURSE",
            allowableValues = {"SAVED_COURSE", "RECOMMENDED_REGION"})
        String itemType,

        @Schema(description = "식별자(저장 코스 ID 또는 추천 지역 ID)", example = "1")
        Long id,

        @Schema(description = "제목(코스명 또는 지역명)")
        String title,

        @Schema(description = "대표 이미지 URL")
        String imageUrl,

        @Schema(description = "부가 정보. SAVED_COURSE: 상태값, RECOMMENDED_REGION: 추천 이유.", nullable = true)
        String subtitle
    ) {
    }

    public static HomeFeedResponse of(List<InnerFeedItem> items) {
        return new HomeFeedResponse(items);
    }
}
