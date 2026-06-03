package live.lbtrip.domain.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import live.lbtrip.domain.user.model.User;
import live.lbtrip.domain.user.model.UserStatus;

public record SignupResponse(
	@Schema(description = "가입된 사용자 ID", example = "1")
	Long userId,

	@Schema(description = "가입된 이메일", example = "user@example.com")
	String email,

	@Schema(description = "사용자 상태. 회원가입 직후에는 PENDING_EMAIL_VERIFICATION, 인증 완료 후 ACTIVE입니다.", example = "PENDING_EMAIL_VERIFICATION")
	UserStatus status
) {
	public static SignupResponse from(User user) {
		return new SignupResponse(user.getId(), user.getEmail(), user.getStatus());
	}
}
