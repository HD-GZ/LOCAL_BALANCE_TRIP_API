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
    UserStatus status,

    @Schema(description = "이메일 인증번호 만료까지 남은 시간(초)", example = "86400")
    Long verificationCodeExpiresIn
) {
    public static SignupResponse from(User user, long verificationCodeExpiresIn) {
        return new SignupResponse(user.getId(), user.getEmail(), user.getStatus(), verificationCodeExpiresIn);
    }
}
