package live.lbtrip.domain.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record PasswordResetTokenResponse(
    @Schema(description = "새 비밀번호 설정 요청에 사용하는 일회용 리셋 토큰", example = "11111111-1111-1111-1111-111111111111")
    String resetToken,

    @Schema(description = "리셋 토큰 만료까지 남은 시간(초)", example = "600")
    long expiresIn
) {
    public static PasswordResetTokenResponse of(String resetToken, long expiresIn) {
        return new PasswordResetTokenResponse(resetToken, expiresIn);
    }
}
