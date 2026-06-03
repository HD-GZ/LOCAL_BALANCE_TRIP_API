package live.lbtrip.domain.user.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import live.lbtrip.domain.user.dto.response.EmailAvailabilityResponse;

@Tag(name = "User", description = "사용자 API")
public interface UserApi {

    @Operation(summary = "이메일 사용 가능 여부 확인", description = "가입 전 이메일 중복 여부를 확인합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            examples = @ExampleObject(value = """
                {
                  "result": "SUCCESS",
                  "data": {
                    "available": true
                  },
                  "error": null
                }
                """)
        )),
        @ApiResponse(responseCode = "400", description = "입력값 오류", content = @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            examples = @ExampleObject(value = """
                {
                  "result": "ERROR",
                  "data": null,
                  "error": {
                    "code": "INVALID_INPUT_VALUE",
                    "message": "입력값이 올바르지 않습니다.",
                    "data": [
                      {
                        "field": "email",
                        "message": "이메일 형식이 올바르지 않습니다."
                      }
                    ]
                  }
                }
                """)
        ))
    })
    ResponseEntity<live.lbtrip.global.response.ApiResponse<EmailAvailabilityResponse>> checkEmailAvailability(
        @Parameter(description = "중복 확인할 이메일", example = "user@example.com", required = true)
        @RequestParam String email
    );
}
