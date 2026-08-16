package live.lbtrip.domain.auth.dto.request;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
    @Schema(description = "가입한 이메일", example = "user@example.com", requiredMode = REQUIRED)
    @NotBlank(message = "{validation.email.required}")
    @Email(message = "{validation.email.format}")
    String email,

    @Schema(description = "비밀번호", example = "password123", requiredMode = REQUIRED)
    @NotBlank(message = "{validation.password.required}")
    String password
) {
}
