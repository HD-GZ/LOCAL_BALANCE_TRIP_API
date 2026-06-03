package live.lbtrip.domain.auth.dto.response;

import live.lbtrip.domain.user.model.User;
import live.lbtrip.domain.user.model.UserStatus;

public record EmailVerificationResponse(
	Long userId,
	String email,
	UserStatus status
) {
	public static EmailVerificationResponse from(User user) {
		return new EmailVerificationResponse(user.getId(), user.getEmail(), user.getStatus());
	}
}
