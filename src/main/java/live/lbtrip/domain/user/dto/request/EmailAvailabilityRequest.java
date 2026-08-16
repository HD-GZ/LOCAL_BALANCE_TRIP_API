package live.lbtrip.domain.user.dto.request;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record EmailAvailabilityRequest(
    @Parameter(description = "중복 확인할 이메일", example = "user@example.com", required = true)
    @NotBlank(message = "{validation.email.required}")
    @Email(message = "{validation.email.format}")
    String email
) {
}
