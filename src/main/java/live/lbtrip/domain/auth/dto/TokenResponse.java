package live.lbtrip.domain.auth.dto;

public record TokenResponse(
	String tokenType,
	String accessToken,
	String refreshToken,
	long accessTokenExpiresIn
) {
	public static TokenResponse of(String accessToken, String refreshToken, long accessTokenExpiresIn) {
		return new TokenResponse("Bearer", accessToken, refreshToken, accessTokenExpiresIn);
	}
}
