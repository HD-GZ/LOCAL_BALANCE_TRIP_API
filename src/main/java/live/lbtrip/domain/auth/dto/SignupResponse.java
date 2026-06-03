package live.lbtrip.domain.auth.dto;

import live.lbtrip.domain.user.User;
import live.lbtrip.domain.user.UserStatus;

public record SignupResponse(
	Long userId,
	String email,
	UserStatus status
) {
	public static SignupResponse from(User user) {
		return new SignupResponse(user.getId(), user.getEmail(), user.getStatus());
	}
}
