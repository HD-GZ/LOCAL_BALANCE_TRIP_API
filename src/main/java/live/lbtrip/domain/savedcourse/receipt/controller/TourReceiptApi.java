package live.lbtrip.domain.savedcourse.receipt.controller;

import static live.lbtrip.global.error.ErrorCode.*;
import static org.springframework.http.HttpStatus.CREATED;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import live.lbtrip.domain.savedcourse.receipt.dto.request.TourReceiptCreateRequest;
import live.lbtrip.domain.savedcourse.receipt.dto.response.ReceiptScanResponse;
import live.lbtrip.domain.savedcourse.receipt.dto.response.TourReceiptDownloadUrlResponse;
import live.lbtrip.domain.savedcourse.receipt.dto.response.TourReceiptListResponse;
import live.lbtrip.domain.savedcourse.receipt.dto.response.TourReceiptResponse;
import live.lbtrip.global.swagger.ApiErrorCodeResponses;
import live.lbtrip.global.swagger.ApiSuccessResponse;
import live.lbtrip.global.web.UserId;

@Tag(name = "TourReceipt", description = "영수증 OCR·환급 증빙 API")
public interface TourReceiptApi {

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "영수증 스캔",
        description = """
            영수증 이미지를 업로드하고 OCR로 가맹점명·결제 금액·결제 일자를 추출합니다.
            이미지는 즉시 저장되며, 응답의 imageId를 환급 증빙 등록 요청에 전달해야 합니다.
            OCR 인식에 실패한 필드는 null로 반환되므로 사용자가 직접 입력한 값을 등록 요청에 전달해야 합니다.
            지원 형식은 jpg, jpeg, png, webp이며, 기본 최대 크기는 10MB로 환경별 설정에 따라 달라질 수 있습니다.
            """
    )
    @ApiSuccessResponse(description = "영수증 스캔 성공")
    @ApiErrorCodeResponses({
        INVALID_ACCESS_TOKEN,
        SAVED_COURSE_NOT_FOUND,
        USER_NOT_FOUND,
        INVALID_INPUT_VALUE,
        INVALID_IMAGE_TYPE,
        IMAGE_SIZE_EXCEEDED,
        IMAGE_UPLOAD_FAILED
    })
    ResponseEntity<ReceiptScanResponse> scanReceipt(
        @UserId Long userId,
        @Parameter(description = "저장 코스 식별자", example = "1") @PathVariable Long savedCourseId,
        @Parameter(description = "영수증 이미지 파일") MultipartFile image
    );

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "환급 증빙 등록",
        description = """
            영수증 스캔 응답의 imageId와 사용자가 확인·수정한 가맹점명, 결제 금액, 결제 일자로 환급 증빙을 등록합니다.
            등록된 환급 증빙 정보와 영수증 이미지 URL을 반환합니다.
            """
    )
    @ApiSuccessResponse(status = CREATED, description = "환급 증빙 등록 성공")
    @ApiErrorCodeResponses({
        INVALID_ACCESS_TOKEN,
        SAVED_COURSE_NOT_FOUND,
        IMAGE_NOT_FOUND,
        IMAGE_ALREADY_ATTACHED,
        INVALID_INPUT_VALUE
    })
    ResponseEntity<TourReceiptResponse> createReceipt(
        @UserId Long userId,
        @Parameter(description = "저장 코스 식별자", example = "1") @PathVariable Long savedCourseId,
        @Valid TourReceiptCreateRequest request
    );

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "환급 증빙 목록 조회",
        description = """
            저장 코스의 환급 증빙 목록을 최신순으로 조회합니다.
            증빙 금액 합계(예상 환급 기준 금액)를 함께 반환합니다.
            """
    )
    @ApiSuccessResponse(description = "환급 증빙 목록 조회 성공")
    @ApiErrorCodeResponses({
        INVALID_ACCESS_TOKEN,
        SAVED_COURSE_NOT_FOUND
    })
    ResponseEntity<TourReceiptListResponse> getReceipts(
        @UserId Long userId,
        @Parameter(description = "저장 코스 식별자", example = "1") @PathVariable Long savedCourseId
    );

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "환급 증빙 상세 조회",
        description = """
            환급 증빙의 상세 정보를 조회합니다.
            가맹점명, 결제 금액, 결제 일자와 영수증 원본 이미지 URL을 반환합니다.
            """
    )
    @ApiSuccessResponse(description = "환급 증빙 상세 조회 성공")
    @ApiErrorCodeResponses({
        INVALID_ACCESS_TOKEN,
        SAVED_COURSE_NOT_FOUND,
        TOUR_RECEIPT_NOT_FOUND
    })
    ResponseEntity<TourReceiptResponse> getReceipt(
        @UserId Long userId,
        @Parameter(description = "저장 코스 식별자", example = "1") @PathVariable Long savedCourseId,
        @Parameter(description = "환급 증빙 식별자", example = "1") @PathVariable Long receiptId
    );

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "환급 증빙 영수증 다운로드 URL 발급",
        description = """
            영수증 원본 이미지를 파일로 내려받을 수 있는 만료 시간이 있는 presigned URL을 발급합니다.
            발급된 URL로 GET 요청 시 브라우저가 화면 표시 대신 파일 다운로드로 동작합니다.
            URL은 expiresAt 이후 만료되므로 다운로드 시점마다 새로 발급받아야 합니다.
            """
    )
    @ApiSuccessResponse(description = "다운로드 URL 발급 성공")
    @ApiErrorCodeResponses({
        INVALID_ACCESS_TOKEN,
        SAVED_COURSE_NOT_FOUND,
        TOUR_RECEIPT_NOT_FOUND
    })
    ResponseEntity<TourReceiptDownloadUrlResponse> getReceiptDownloadUrl(
        @UserId Long userId,
        @Parameter(description = "저장 코스 식별자", example = "1") @PathVariable Long savedCourseId,
        @Parameter(description = "환급 증빙 식별자", example = "1") @PathVariable Long receiptId
    );

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "환급 증빙 삭제",
        description = """
            환급 증빙을 삭제합니다.
            증빙에 연결된 영수증 원본 이미지도 함께 삭제합니다.
            """
    )
    @ApiSuccessResponse(description = "환급 증빙 삭제 성공")
    @ApiErrorCodeResponses({
        INVALID_ACCESS_TOKEN,
        SAVED_COURSE_NOT_FOUND,
        TOUR_RECEIPT_NOT_FOUND
    })
    ResponseEntity<Void> deleteReceipt(
        @UserId Long userId,
        @Parameter(description = "저장 코스 식별자", example = "1") @PathVariable Long savedCourseId,
        @Parameter(description = "환급 증빙 식별자", example = "1") @PathVariable Long receiptId
    );
}
