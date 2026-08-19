package live.lbtrip.admin.tourism.controller;

import static live.lbtrip.global.error.ErrorCode.INVALID_ADMIN_ACCESS_TOKEN;
import static live.lbtrip.global.error.ErrorCode.INVALID_INPUT_VALUE;
import static live.lbtrip.global.error.ErrorCode.TOUR_SYNC_IN_PROGRESS;
import static org.springframework.http.HttpStatus.ACCEPTED;

import org.springframework.http.ResponseEntity;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import live.lbtrip.domain.tourism.model.enums.TourSyncStep;
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

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "관광 데이터 동기화 단계 실행",
        description = """
            동기화의 특정 단계만 백그라운드로 실행합니다.
            공공데이터 API 일일 한도를 초과했을 때 필요한 단계만 골라 재실행하는 용도입니다.

            - REGIONS: 지역별 통계·장소·오디오 테마 적재 (지역당 API 6회)
            - PLACE_THEMES: 장소-오디오 테마 매칭 (외부 API 호출 없음)
            - OVERVIEWS: 장소 상세 설명 보강 (미적재 장소당 API 1회)
            - AUDIO_URLS: 오디오 테마 음원 URL 보강 (미적재 테마당 API 1회)
            - VISITOR_STATS: 지역별 방문자수 적재 (미적재 일자당 API 1회, 최대 45회)
            - PLACES_EN: 지역별 영문 장소 적재 (지역당 API 5회)
            - OVERVIEWS_EN: 영문 장소 상세 설명 보강 (미적재 영문 장소당 API 1회)
            - REGION_NAMES_EN: 지역 영문명 적재 (시도당 API 1회)

            실행 중 일일 한도를 초과하면 해당 단계를 즉시 중단합니다.
            미적재 대상만 조회하므로 다음 날 다시 실행하면 남은 분부터 이어서 진행됩니다.
            """
    )
    @ApiSuccessResponse(status = ACCEPTED, description = "단계 실행 시작됨")
    @ApiErrorCodeResponses({
        INVALID_INPUT_VALUE,
        INVALID_ADMIN_ACCESS_TOKEN,
        TOUR_SYNC_IN_PROGRESS
    })
    ResponseEntity<Void> triggerStepSync(
        @AdminId Long adminId,
        @Parameter(description = "실행할 동기화 단계", required = true) TourSyncStep step
    );
}
