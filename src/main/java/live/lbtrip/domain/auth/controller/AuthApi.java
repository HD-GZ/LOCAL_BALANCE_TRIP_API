package live.lbtrip.domain.auth.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import live.lbtrip.domain.auth.dto.request.EmailVerificationConfirmRequest;
import live.lbtrip.domain.auth.dto.request.EmailVerificationResendRequest;
import live.lbtrip.domain.auth.dto.request.LoginRequest;
import live.lbtrip.domain.auth.dto.request.SignupRequest;
import live.lbtrip.domain.auth.dto.request.TokenRefreshRequest;
import live.lbtrip.domain.auth.dto.response.EmailVerificationResponse;
import live.lbtrip.domain.auth.dto.response.LoginResponse;
import live.lbtrip.domain.auth.dto.response.SignupResponse;
import live.lbtrip.domain.auth.dto.response.TokenResponse;
import live.lbtrip.global.web.UserId;

@Tag(name = "Auth", description = "회원가입, 로그인, 토큰, 이메일 인증 API")
public interface AuthApi {

    String VALIDATION_ERROR_EXAMPLE = """
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
        """;

    @Operation(
        summary = "회원가입",
        description = "회원가입 후 6자리 이메일 인증 코드를 발송합니다. 요청 body의 gender는 MALE, FEMALE, NOT_SPECIFIED 중 하나입니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "회원가입 성공", content = @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            examples = @ExampleObject(value = """
                {
                  "result": "SUCCESS",
                  "data": {
                    "userId": 1,
                    "email": "user@example.com",
                    "status": "PENDING_EMAIL_VERIFICATION",
                    "verificationCodeExpiresIn": 86400
                  },
                  "error": null
                }
                """)
        )),
        @ApiResponse(responseCode = "400", description = "입력값 오류 또는 필수 약관 미동의", content = @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            examples = {
                @ExampleObject(name = "유효성 검증 오류", value = VALIDATION_ERROR_EXAMPLE),
                @ExampleObject(name = "비밀번호 확인 불일치", value = """
                    {
                      "result": "ERROR",
                      "data": null,
                      "error": {
                        "code": "PASSWORD_CONFIRM_MISMATCH",
                        "message": "비밀번호와 비밀번호 확인이 일치하지 않습니다.",
                        "data": null
                      }
                    }
                    """),
                @ExampleObject(name = "필수 약관 미동의", value = """
                    {
                      "result": "ERROR",
                      "data": null,
                      "error": {
                        "code": "REQUIRED_AGREEMENT_NOT_ACCEPTED",
                        "message": "필수 약관에 동의해야 합니다.",
                        "data": null
                      }
                    }
                    """)
            }
        )),
        @ApiResponse(responseCode = "409", description = "이미 사용 중인 이메일", content = @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            examples = @ExampleObject(value = """
                {
                  "result": "ERROR",
                  "data": null,
                  "error": {
                    "code": "DUPLICATE_EMAIL",
                    "message": "이미 사용 중인 이메일입니다.",
                    "data": null
                  }
                }
                """)
        ))
    })
    ResponseEntity<live.lbtrip.global.response.ApiResponse<SignupResponse>> signup(
        @Valid @RequestBody SignupRequest request
    );

    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인하고 access token과 refresh token을 반환합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "로그인 성공", content = @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            examples = @ExampleObject(value = """
                {
                  "result": "SUCCESS",
                  "data": {
                    "accessToken": "eyJhbGciOiJIUzI1NiJ9.access",
                    "refreshToken": "eyJhbGciOiJIUzI1NiJ9.refresh"
                  },
                  "error": null
                }
                """)
        )),
        @ApiResponse(responseCode = "400", description = "입력값 오류", content = @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            examples = @ExampleObject(value = VALIDATION_ERROR_EXAMPLE)
        )),
        @ApiResponse(responseCode = "401", description = "이메일 또는 비밀번호 불일치", content = @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            examples = @ExampleObject(value = """
                {
                  "result": "ERROR",
                  "data": null,
                  "error": {
                    "code": "INVALID_LOGIN_CREDENTIALS",
                    "message": "이메일 또는 비밀번호가 일치하지 않습니다.",
                    "data": null
                  }
                }
                """)
        )),
        @ApiResponse(responseCode = "403", description = "이메일 미인증", content = @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            examples = @ExampleObject(value = """
                {
                  "result": "ERROR",
                  "data": null,
                  "error": {
                    "code": "EMAIL_NOT_VERIFIED",
                    "message": "이메일 인증이 필요합니다.",
                    "data": null
                  }
                }
                """)
        ))
    })
    ResponseEntity<live.lbtrip.global.response.ApiResponse<LoginResponse>> login(
        @Valid @RequestBody LoginRequest request
    );

    @Operation(summary = "토큰 갱신", description = "refresh token으로 새 access token을 발급합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "토큰 갱신 성공", content = @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            examples = @ExampleObject(value = """
                {
                  "result": "SUCCESS",
                  "data": {
                    "accessToken": "eyJhbGciOiJIUzI1NiJ9.new-access",
                    "refreshToken": "eyJhbGciOiJIUzI1NiJ9.refresh"
                  },
                  "error": null
                }
                """)
        )),
        @ApiResponse(responseCode = "400", description = "입력값 오류", content = @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            examples = @ExampleObject(value = VALIDATION_ERROR_EXAMPLE)
        )),
        @ApiResponse(responseCode = "401", description = "유효하지 않거나 만료된 refresh token", content = @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            examples = {
                @ExampleObject(name = "유효하지 않은 리프레시 토큰", value = """
                    {
                      "result": "ERROR",
                      "data": null,
                      "error": {
                        "code": "INVALID_REFRESH_TOKEN",
                        "message": "유효하지 않은 리프레시 토큰입니다.",
                        "data": null
                      }
                    }
                    """),
                @ExampleObject(name = "만료된 리프레시 토큰", value = """
                    {
                      "result": "ERROR",
                      "data": null,
                      "error": {
                        "code": "EXPIRED_REFRESH_TOKEN",
                        "message": "만료된 리프레시 토큰입니다.",
                        "data": null
                      }
                    }
                    """)
            }
        ))
    })
    ResponseEntity<live.lbtrip.global.response.ApiResponse<TokenResponse>> refreshToken(
        @Valid @RequestBody TokenRefreshRequest request
    );

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "로그아웃", description = "로그인한 사용자를 로그아웃 처리합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "로그아웃 성공", content = @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            examples = @ExampleObject(value = """
                {
                  "result": "SUCCESS",
                  "data": null,
                  "error": null
                }
                """)
        )),
        @ApiResponse(responseCode = "401", description = "유효하지 않은 access token", content = @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            examples = @ExampleObject(value = """
                {
                  "result": "ERROR",
                  "data": null,
                  "error": {
                    "code": "INVALID_ACCESS_TOKEN",
                    "message": "유효하지 않은 액세스 토큰입니다.",
                    "data": null
                  }
                }
                """)
        ))
    })
    ResponseEntity<live.lbtrip.global.response.ApiResponse<Object>> logout(@UserId Long userId);

    @Operation(summary = "이메일 인증 확인", description = "6자리 이메일 인증 코드를 확인하고 사용자를 활성화합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "이메일 인증 성공", content = @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            examples = @ExampleObject(value = """
                {
                  "result": "SUCCESS",
                  "data": {
                    "userId": 1,
                    "email": "user@example.com",
                    "status": "ACTIVE"
                  },
                  "error": null
                }
                """)
        )),
        @ApiResponse(responseCode = "400", description = "입력값 오류, 만료 또는 이미 사용된 코드", content = @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            examples = {
                @ExampleObject(name = "유효성 검증 오류", value = VALIDATION_ERROR_EXAMPLE),
                @ExampleObject(name = "만료된 인증 코드", value = """
                    {
                      "result": "ERROR",
                      "data": null,
                      "error": {
                        "code": "EMAIL_VERIFICATION_CODE_EXPIRED",
                        "message": "이메일 인증 코드가 만료되었습니다.",
                        "data": null
                      }
                    }
                    """),
                @ExampleObject(name = "이미 사용된 인증 코드", value = """
                    {
                      "result": "ERROR",
                      "data": null,
                      "error": {
                        "code": "EMAIL_VERIFICATION_CODE_USED",
                        "message": "이미 사용된 이메일 인증 코드입니다.",
                        "data": null
                      }
                    }
                    """)
            }
        )),
        @ApiResponse(responseCode = "404", description = "인증 코드 없음", content = @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            examples = @ExampleObject(value = """
                {
                  "result": "ERROR",
                  "data": null,
                  "error": {
                    "code": "EMAIL_VERIFICATION_CODE_NOT_FOUND",
                    "message": "이메일 인증 코드를 찾을 수 없습니다.",
                    "data": null
                  }
                }
                """)
        ))
    })
    ResponseEntity<live.lbtrip.global.response.ApiResponse<EmailVerificationResponse>> confirmEmailVerification(
        @Valid @RequestBody EmailVerificationConfirmRequest request
    );

    @Operation(summary = "이메일 인증 코드 재발송", description = "미인증 사용자에게 6자리 이메일 인증 코드를 다시 발송합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "인증 코드 재발송 성공", content = @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            examples = @ExampleObject(value = """
                {
                  "result": "SUCCESS",
                  "data": {
                    "userId": 1,
                    "email": "user@example.com",
                    "status": "PENDING_EMAIL_VERIFICATION"
                  },
                  "error": null
                }
                """)
        )),
        @ApiResponse(responseCode = "400", description = "입력값 오류", content = @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            examples = @ExampleObject(value = VALIDATION_ERROR_EXAMPLE)
        )),
        @ApiResponse(responseCode = "404", description = "사용자 없음", content = @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            examples = @ExampleObject(value = """
                {
                  "result": "ERROR",
                  "data": null,
                  "error": {
                    "code": "USER_NOT_FOUND",
                    "message": "사용자를 찾을 수 없습니다.",
                    "data": null
                  }
                }
                """)
        )),
        @ApiResponse(responseCode = "409", description = "이미 인증 완료된 이메일", content = @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            examples = @ExampleObject(value = """
                {
                  "result": "ERROR",
                  "data": null,
                  "error": {
                    "code": "EMAIL_ALREADY_VERIFIED",
                    "message": "이미 이메일 인증이 완료된 계정입니다.",
                    "data": null
                  }
                }
                """)
        ))
    })
    ResponseEntity<live.lbtrip.global.response.ApiResponse<EmailVerificationResponse>> resendEmailVerification(
        @Valid @RequestBody EmailVerificationResendRequest request
    );
}
