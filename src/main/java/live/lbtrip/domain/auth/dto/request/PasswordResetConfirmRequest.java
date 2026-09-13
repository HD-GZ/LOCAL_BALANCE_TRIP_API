package live.lbtrip.domain.auth.dto.request;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PasswordResetConfirmRequest(
    @Schema(description = "비밀번호를 재설정할 계정의 이메일", example = "user@example.com", requiredMode = REQUIRED)
    @NotBlank(message = "{validation.email.required}")
    @Email(message = "{validation.email.format}")
    @Size(max = 255, message = "{validation.email.size}")
    String email,

    @Schema(description = "이메일로 발송된 6자리 인증 코드", example = "123456", requiredMode = REQUIRED)
    @NotBlank(message = "{validation.verificationCode.required}")
    @Pattern(regexp = "^\\d{6}$", message = "{validation.verificationCode.pattern}")
    String code
) {
}
