package live.lbtrip.domain.auth.dto.request;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record EmailVerificationConfirmRequest(
    @Schema(description = "이메일로 발송된 6자리 인증 코드", example = "123456", requiredMode = REQUIRED)
    @NotBlank(message = "{validation.verificationCode.required}")
    @Pattern(regexp = "^\\d{6}$", message = "{validation.verificationCode.pattern}")
    String code
) {
}
