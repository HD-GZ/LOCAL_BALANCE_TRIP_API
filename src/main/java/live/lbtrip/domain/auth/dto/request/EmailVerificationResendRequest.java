package live.lbtrip.domain.auth.dto.request;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record EmailVerificationResendRequest(
    @Schema(description = "인증 코드를 재발송할 이메일", example = "user@example.com", requiredMode = REQUIRED)
    @NotBlank(message = "{validation.email.required}")
    @Email(message = "{validation.email.format}")
    String email
) {
}
