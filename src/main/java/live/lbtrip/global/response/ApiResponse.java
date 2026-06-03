package live.lbtrip.global.response;

import static live.lbtrip.global.response.ResultType.ERROR;
import static live.lbtrip.global.response.ResultType.SUCCESS;

import io.swagger.v3.oas.annotations.media.Schema;
import live.lbtrip.global.error.ErrorCode;
import live.lbtrip.global.error.ErrorMessage;

public record ApiResponse<T>(
    @Schema(description = "응답 결과 타입. 성공은 SUCCESS, 실패는 ERROR입니다.", example = "SUCCESS")
    ResultType result,

    @Schema(description = "성공 응답 데이터. 실패 시 null입니다.")
    T data,

    @Schema(description = "실패 응답 정보. 성공 시 null입니다.")
    ErrorMessage error
) {

    public static ApiResponse<Object> success() {
        return new ApiResponse<>(SUCCESS, null, null);
    }

    public static <S> ApiResponse<S> success(S data) {
        return new ApiResponse<>(SUCCESS, data, null);
    }

    public static <S> ApiResponse<S> error(ErrorCode errorCode) {
        return error(errorCode, null);
    }

    public static <S> ApiResponse<S> error(ErrorCode errorCode, Object errorData) {
        return new ApiResponse<>(ERROR, null, ErrorMessage.of(errorCode, errorData));
    }
}
