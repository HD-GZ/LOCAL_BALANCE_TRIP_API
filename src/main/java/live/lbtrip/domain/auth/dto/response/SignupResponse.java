package live.lbtrip.domain.auth.dto.response;

import live.lbtrip.domain.user.model.User;
import live.lbtrip.domain.user.model.UserStatus;

public record SignupResponse(
	Long userId,
	String email,
	UserStatus status
) {
	public static SignupResponse from(User user) {
		return new SignupResponse(user.getId(), user.getEmail(), user.getStatus());
	}
}
