package live.lbtrip.domain.home.controller;

import static live.lbtrip.global.error.ErrorCode.INTERNAL_SERVER_ERROR;

import org.springframework.http.ResponseEntity;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import live.lbtrip.domain.home.dto.response.ProfileTypeListResponse;
import live.lbtrip.global.swagger.ApiErrorCodeResponses;
import live.lbtrip.global.swagger.ApiSuccessResponse;

@Tag(name = "Home", description = "홈 화면 섹션 API")
public interface HomeApi {

    @Operation(
        summary = "취향 진단 대표 유형 목록",
        description = "비로그인 홈 히어로에 노출할 대표 취향 진단 유형 목록을 조회합니다."
    )
    @ApiSuccessResponse(description = "대표 유형 목록 조회 성공")
    @ApiErrorCodeResponses({
        INTERNAL_SERVER_ERROR
    })
    ResponseEntity<ProfileTypeListResponse> getProfileTypes();
}
