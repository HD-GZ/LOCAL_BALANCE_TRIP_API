package live.lbtrip.global.response;

import static live.lbtrip.global.response.ResultType.ERROR;
import static live.lbtrip.global.response.ResultType.SUCCESS;

import live.lbtrip.global.error.ErrorCode;
import live.lbtrip.global.error.ErrorMessage;

public record ApiResponse<T>(
    ResultType result,
    T data,
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
