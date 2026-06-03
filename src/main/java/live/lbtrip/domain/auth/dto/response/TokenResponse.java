package live.lbtrip.domain.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record TokenResponse(
	@Schema(description = "새로 발급된 access token", example = "eyJhbGciOiJIUzI1NiJ9.new-access")
	String accessToken,

	@Schema(description = "기존 refresh token", example = "eyJhbGciOiJIUzI1NiJ9.refresh")
	String refreshToken
) {
	public static TokenResponse of(String accessToken, String refreshToken) {
		return new TokenResponse(accessToken, refreshToken);
	}
}
