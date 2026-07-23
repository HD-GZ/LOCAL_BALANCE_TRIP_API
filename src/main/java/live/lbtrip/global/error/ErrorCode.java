package live.lbtrip.global.error;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public enum ErrorCode {

    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "입력값이 올바르지 않습니다."),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 리소스를 찾을 수 없습니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
    PASSWORD_CONFIRM_MISMATCH(HttpStatus.BAD_REQUEST, "비밀번호와 비밀번호 확인이 일치하지 않습니다."),
    REQUIRED_AGREEMENT_NOT_ACCEPTED(HttpStatus.BAD_REQUEST, "필수 약관에 동의해야 합니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    EMAIL_ALREADY_VERIFIED(HttpStatus.CONFLICT, "이미 이메일 인증이 완료된 계정입니다."),
    EMAIL_VERIFICATION_CODE_NOT_FOUND(HttpStatus.NOT_FOUND, "이메일 인증 코드를 찾을 수 없습니다."),
    EMAIL_VERIFICATION_CODE_EXPIRED(HttpStatus.BAD_REQUEST, "이메일 인증 코드가 만료되었습니다."),
    EMAIL_VERIFICATION_CODE_USED(HttpStatus.BAD_REQUEST, "이미 사용된 이메일 인증 코드입니다."),
    EMAIL_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "이메일 발송에 실패했습니다."),
    INVALID_LOGIN_CREDENTIALS(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 일치하지 않습니다."),
    EMAIL_NOT_VERIFIED(HttpStatus.FORBIDDEN, "이메일 인증이 필요합니다."),
    INVALID_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 액세스 토큰입니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 리프레시 토큰입니다."),
    EXPIRED_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 리프레시 토큰입니다."),
    PROPENSITY_NOT_FOUND(HttpStatus.NOT_FOUND, "취향 진단 결과를 찾을 수 없습니다."),
    TRAVEL_PROFILE_NOT_FOUND(HttpStatus.INTERNAL_SERVER_ERROR, "여행 프로필 정보를 찾을 수 없습니다."),
    REGION_NOT_FOUND(HttpStatus.NOT_FOUND, "추천 지역을 찾을 수 없습니다."),
    COURSE_NOT_FOUND(HttpStatus.NOT_FOUND, "추천 코스를 찾을 수 없습니다."),
    SAVED_COURSE_NOT_FOUND(HttpStatus.NOT_FOUND, "저장한 코스를 찾을 수 없습니다."),
    TOUR_API_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "관광 정보 API 호출에 실패했습니다."),
    TOUR_DATA_NOT_READY(HttpStatus.SERVICE_UNAVAILABLE, "관광 데이터가 아직 준비되지 않았습니다."),
    INVALID_ADMIN_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 어드민 액세스 토큰입니다."),
    INCENTIVE_NOT_FOUND(HttpStatus.NOT_FOUND, "인센티브를 찾을 수 없습니다."),
    INCENTIVE_REGION_INVALID(HttpStatus.BAD_REQUEST, "존재하지 않는 지역 코드입니다."),
    RECOMMENDATION_GENERATION_FAILED(HttpStatus.SERVICE_UNAVAILABLE, "코스 추천 생성에 실패했습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다."),
    ;

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
