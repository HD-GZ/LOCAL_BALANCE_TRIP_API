package live.lbtrip.domain.auth.dto.request;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record TokenRefreshRequest(
    @Schema(description = "로그인 또는 이전 갱신 시 발급받은 refresh token", example = "eyJhbGciOiJIUzI1NiJ9.refresh", requiredMode = REQUIRED)
    @NotBlank(message = "리프레시 토큰은 필수입니다.")
    String refreshToken
) {
}
