package live.lbtrip.global.error;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import live.lbtrip.global.i18n.MessageResolver;
import live.lbtrip.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final MessageResolver messageResolver;

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Object>> handleBusinessException(BusinessException exception) {
        ErrorCode errorCode = exception.getErrorCode();
        if (errorCode.getStatus().is5xxServerError()) {
            log.error(
                "Business exception: errorCode={}, status={}",
                errorCode.name(),
                errorCode.getStatus().value(),
                exception
            );
        }
        return error(errorCode);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleMethodArgumentNotValidException(
        MethodArgumentNotValidException exception
    ) {
        List<FieldErrorDetail> errors = exception.getBindingResult().getFieldErrors().stream()
            .map(error -> FieldErrorDetail.of(error.getField(), error.getDefaultMessage()))
            .toList();

        return error(ErrorCode.INVALID_INPUT_VALUE, errors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Object>> handleHttpMessageNotReadableException() {
        return error(ErrorCode.INVALID_INPUT_VALUE);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Object>> handleMethodArgumentTypeMismatchException() {
        return error(ErrorCode.INVALID_INPUT_VALUE);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleNoResourceFoundException() {
        return error(ErrorCode.RESOURCE_NOT_FOUND);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Object>> handleMaxUploadSizeExceededException() {
        return error(ErrorCode.IMAGE_SIZE_EXCEEDED);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleException(Exception exception) {
        log.error("Unhandled exception", exception);
        return error(ErrorCode.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ApiResponse<Object>> error(ErrorCode errorCode) {
        return error(errorCode, null);
    }

    private ResponseEntity<ApiResponse<Object>> error(ErrorCode errorCode, Object errorData) {
        return ResponseEntity.status(errorCode.getStatus())
            .header(HttpHeaders.CONTENT_LANGUAGE, messageResolver.currentLocale().getLanguage())
            .body(ApiResponse.error(errorCode, messageResolver.resolve(errorCode), errorData));
    }
}
