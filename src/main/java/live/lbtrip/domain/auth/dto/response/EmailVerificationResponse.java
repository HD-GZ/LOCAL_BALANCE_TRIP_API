package live.lbtrip.domain.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import live.lbtrip.domain.user.model.User;
import live.lbtrip.domain.user.model.UserStatus;

public record EmailVerificationResponse(
    @Schema(description = "사용자 ID", example = "1")
    Long userId,

    @Schema(description = "이메일 인증 대상 이메일", example = "user@example.com")
    String email,

    @Schema(description = "사용자 상태. 인증 대기 상태는 PENDING_EMAIL_VERIFICATION, 인증 완료 상태는 ACTIVE입니다.", example = "ACTIVE")
    UserStatus status
) {
    public static EmailVerificationResponse from(User user) {
        return new EmailVerificationResponse(user.getId(), user.getEmail(), user.getStatus());
    }
}
