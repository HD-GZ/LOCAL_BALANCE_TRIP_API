package live.lbtrip.admin.tourism.controller;

import static live.lbtrip.global.error.ErrorCode.INVALID_ADMIN_ACCESS_TOKEN;
import static live.lbtrip.global.error.ErrorCode.TOUR_SYNC_IN_PROGRESS;
import static org.springframework.http.HttpStatus.ACCEPTED;

import org.springframework.http.ResponseEntity;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import live.lbtrip.global.swagger.ApiErrorCodeResponses;
import live.lbtrip.global.swagger.ApiSuccessResponse;
import live.lbtrip.global.web.AdminId;

@Tag(name = "Admin Tour Sync", description = "어드민 관광 데이터 동기화 API")
public interface AdminTourSyncApi {

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "관광 데이터 동기화 실행",
        description = """
            관광 통계·장소·오디오 테마·방문자수 데이터 전체 동기화(syncAll)를 백그라운드로 시작합니다.
            시작 즉시 202를 반환하며, 완료까지 수 분이 걸릴 수 있습니다. 진행 상황은 서버 로그로 확인합니다.
            이미 동기화가 실행 중이면 409를 반환합니다.
            """
    )
    @ApiSuccessResponse(status = ACCEPTED, description = "동기화 시작됨")
    @ApiErrorCodeResponses({
        INVALID_ADMIN_ACCESS_TOKEN,
        TOUR_SYNC_IN_PROGRESS
    })
    ResponseEntity<Void> triggerSync(
        @AdminId Long adminId
    );
}
