package live.lbtrip.domain.admin.incentive.controller;

import static live.lbtrip.global.error.ErrorCode.INCENTIVE_NOT_FOUND;
import static live.lbtrip.global.error.ErrorCode.INVALID_ADMIN_ACCESS_TOKEN;
import static live.lbtrip.global.error.ErrorCode.INVALID_INPUT_VALUE;
import static org.springframework.http.HttpStatus.CREATED;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import live.lbtrip.domain.admin.incentive.dto.request.IncentiveRequest;
import live.lbtrip.domain.admin.incentive.dto.response.IncentiveResponse;
import live.lbtrip.global.swagger.ApiErrorCodeResponses;
import live.lbtrip.global.swagger.ApiSuccessResponse;
import live.lbtrip.global.web.AdminId;

@Tag(name = "Admin Incentive", description = "어드민 인센티브 관리 API")
public interface IncentiveApi {

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "인센티브 등록", description = "인센티브 행사 제목과 페이지 URL을 등록합니다.")
    @ApiSuccessResponse(status = CREATED, description = "인센티브 등록 성공")
    @ApiErrorCodeResponses({
        INVALID_INPUT_VALUE,
        INVALID_ADMIN_ACCESS_TOKEN
    })
    ResponseEntity<IncentiveResponse> createIncentive(
        @AdminId Long adminId,
        @Valid @RequestBody IncentiveRequest request
    );

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "인센티브 목록 조회", description = "등록된 인센티브 전체 목록을 조회합니다.")
    @ApiSuccessResponse(description = "인센티브 목록 조회 성공")
    @ApiErrorCodeResponses(INVALID_ADMIN_ACCESS_TOKEN)
    ResponseEntity<List<IncentiveResponse>> getIncentives(@AdminId Long adminId);

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "인센티브 수정", description = "인센티브 행사 제목과 페이지 URL을 수정합니다.")
    @ApiSuccessResponse(description = "인센티브 수정 성공")
    @ApiErrorCodeResponses({
        INVALID_INPUT_VALUE,
        INVALID_ADMIN_ACCESS_TOKEN,
        INCENTIVE_NOT_FOUND
    })
    ResponseEntity<IncentiveResponse> updateIncentive(
        @AdminId Long adminId,
        @Parameter(description = "인센티브 ID", example = "1") @PathVariable Long incentiveId,
        @Valid @RequestBody IncentiveRequest request
    );

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "인센티브 삭제", description = "인센티브를 삭제합니다.")
    @ApiSuccessResponse(description = "인센티브 삭제 성공")
    @ApiErrorCodeResponses({
        INVALID_ADMIN_ACCESS_TOKEN,
        INCENTIVE_NOT_FOUND
    })
    ResponseEntity<Void> deleteIncentive(
        @AdminId Long adminId,
        @Parameter(description = "인센티브 ID", example = "1") @PathVariable Long incentiveId
    );
}
