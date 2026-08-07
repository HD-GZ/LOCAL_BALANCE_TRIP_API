package live.lbtrip.domain.home.dto.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

public record HeroResponse(
    @Schema(description = "히어로 사진 목록")
    List<InnerHeroItem> items
) {

    public record InnerHeroItem(
        @Schema(description = "사진 URL")
        String imageUrl,

        @Schema(description = "표시명(장소명 또는 지역명)", example = "전라남도 담양군")
        String title
    ) {
    }

    public static HeroResponse of(List<InnerHeroItem> items) {
        return new HeroResponse(items);
    }
}
