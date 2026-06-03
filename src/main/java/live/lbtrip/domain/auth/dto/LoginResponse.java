package live.lbtrip.domain.auth.dto;

import live.lbtrip.domain.user.User;

public record LoginResponse(
	Long userId,
	String email,
	String name,
	String tokenType,
	String accessToken,
	String refreshToken,
	long accessTokenExpiresIn
) {
	public static LoginResponse of(User user, String accessToken, String refreshToken, long accessTokenExpiresIn) {
		return new LoginResponse(
			user.getId(),
			user.getEmail(),
			user.getName(),
			"Bearer",
			accessToken,
			refreshToken,
			accessTokenExpiresIn
		);
	}
}
