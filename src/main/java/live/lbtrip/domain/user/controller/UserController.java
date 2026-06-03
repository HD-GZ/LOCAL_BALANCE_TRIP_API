package live.lbtrip.domain.user.controller;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import live.lbtrip.domain.user.dto.EmailAvailabilityResponse;
import live.lbtrip.domain.user.service.UserService;
import live.lbtrip.global.response.ApiResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

	private final UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping("/email-availability")
	public ApiResponse<EmailAvailabilityResponse> checkEmailAvailability(
		@RequestParam
		@NotBlank(message = "이메일은 필수입니다.")
		@Email(message = "이메일 형식이 올바르지 않습니다.")
		String email
	) {
		return ApiResponse.success(userService.checkEmailAvailability(email));
	}
}
