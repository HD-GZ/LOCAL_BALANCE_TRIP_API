package live.lbtrip.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record EmailVerificationConfirmRequest(
	@NotBlank(message = "인증 토큰은 필수입니다.")
	String token
) {
}
