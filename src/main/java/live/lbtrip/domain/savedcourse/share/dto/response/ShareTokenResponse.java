package live.lbtrip.domain.savedcourse.share.dto.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

import live.lbtrip.domain.savedcourse.share.model.entity.CourseShareToken;

public record ShareTokenResponse(
    @Schema(description = "공유 토큰(공유 URL 조립은 클라이언트 책임)", example = "550e8400-e29b-41d4-a716-446655440000")
    String token,

    @Schema(description = "토큰 만료 시각", example = "2026-08-19T13:00:00")
    LocalDateTime expiresAt
) {

    public static ShareTokenResponse from(CourseShareToken shareToken) {
        return new ShareTokenResponse(shareToken.getToken(), shareToken.getExpiresAt());
    }
}
