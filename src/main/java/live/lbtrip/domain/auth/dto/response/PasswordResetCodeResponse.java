package live.lbtrip.domain.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record PasswordResetCodeResponse(
    @Schema(description = "인증 코드 만료까지 남은 시간(초)", example = "600")
    long expiresIn
) {
    public static PasswordResetCodeResponse of(long expiresIn) {
        return new PasswordResetCodeResponse(expiresIn);
    }
}
