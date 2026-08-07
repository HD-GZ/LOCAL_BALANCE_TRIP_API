package live.lbtrip.domain.home.dto.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

public record ProfileTypeListResponse(
    @Schema(description = "대표 취향 진단 유형 목록")
    List<InnerProfileType> types
) {

    public record InnerProfileType(
        @Schema(description = "유형 코드", example = "LVEAI")
        String code,

        @Schema(description = "유형 별칭", example = "찐로컬 탐험가")
        String nickname,

        @Schema(description = "유형 설명")
        String description,

        @Schema(description = "유형 캐릭터 이미지 URL")
        String imageUrl
    ) {
    }

    public static ProfileTypeListResponse of(List<InnerProfileType> types) {
        return new ProfileTypeListResponse(types);
    }
}
