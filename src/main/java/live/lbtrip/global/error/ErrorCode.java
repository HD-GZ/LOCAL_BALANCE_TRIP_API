package live.lbtrip.global.error;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public enum ErrorCode {

	INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "입력값이 올바르지 않습니다."),
	DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
	PASSWORD_CONFIRM_MISMATCH(HttpStatus.BAD_REQUEST, "비밀번호와 비밀번호 확인이 일치하지 않습니다."),
	REQUIRED_AGREEMENT_NOT_ACCEPTED(HttpStatus.BAD_REQUEST, "필수 약관에 동의해야 합니다."),
	USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
	EMAIL_ALREADY_VERIFIED(HttpStatus.CONFLICT, "이미 이메일 인증이 완료된 계정입니다."),
	EMAIL_VERIFICATION_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "이메일 인증 토큰을 찾을 수 없습니다."),
	EMAIL_VERIFICATION_TOKEN_EXPIRED(HttpStatus.BAD_REQUEST, "이메일 인증 토큰이 만료되었습니다."),
	EMAIL_VERIFICATION_TOKEN_USED(HttpStatus.BAD_REQUEST, "이미 사용된 이메일 인증 토큰입니다."),
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다."),
	;

	private final HttpStatus status;
	private final String message;

	ErrorCode(HttpStatus status, String message) {
		this.status = status;
		this.message = message;
	}
}
