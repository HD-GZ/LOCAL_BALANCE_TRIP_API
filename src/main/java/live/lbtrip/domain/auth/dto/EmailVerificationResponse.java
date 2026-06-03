package live.lbtrip.domain.auth.dto;

import live.lbtrip.domain.user.User;
import live.lbtrip.domain.user.UserStatus;

public record EmailVerificationResponse(
	Long userId,
	String email,
	UserStatus status
) {
	public static EmailVerificationResponse from(User user) {
		return new EmailVerificationResponse(user.getId(), user.getEmail(), user.getStatus());
	}
}
