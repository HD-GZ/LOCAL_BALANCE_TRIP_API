package live.lbtrip.domain.admin.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record AdminTokenResponse(
    @Schema(description = "어드민 액세스 토큰", example = "eyJhbGciOiJIUzI1NiJ9.admin-access")
    String accessToken
) {

    public static AdminTokenResponse of(String accessToken) {
        return new AdminTokenResponse(accessToken);
    }
}
