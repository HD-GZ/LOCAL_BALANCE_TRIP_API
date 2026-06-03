package live.lbtrip.domain.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record LoginResponse(
	@Schema(description = "API 인증에 사용하는 access token", example = "eyJhbGciOiJIUzI1NiJ9.access")
	String accessToken,

	@Schema(description = "access token 갱신에 사용하는 refresh token", example = "eyJhbGciOiJIUzI1NiJ9.refresh")
	String refreshToken
) {
	public static LoginResponse of(String accessToken, String refreshToken) {
		return new LoginResponse(accessToken, refreshToken);
	}
}
