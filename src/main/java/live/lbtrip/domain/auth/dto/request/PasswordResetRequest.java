package live.lbtrip.domain.auth.dto.request;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PasswordResetRequest(
    @Schema(description = "인증 코드 확인 시 발급된 리셋 토큰", example = "11111111-1111-1111-1111-111111111111", requiredMode = REQUIRED)
    @NotBlank(message = "{validation.resetToken.required}")
    String resetToken,

    @Schema(description = "영문과 숫자를 포함한 8자 이상의 새 비밀번호", example = "newpassword1", requiredMode = REQUIRED)
    @NotBlank(message = "{validation.password.required}")
    @Pattern(
        regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,}$",
        message = "{validation.password.pattern}"
    )
    String newPassword
) {
}
