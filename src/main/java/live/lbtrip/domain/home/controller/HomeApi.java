package live.lbtrip.domain.home.controller;

import static live.lbtrip.global.error.ErrorCode.INTERNAL_SERVER_ERROR;
import static live.lbtrip.global.error.ErrorCode.INVALID_ACCESS_TOKEN;
import static live.lbtrip.global.error.ErrorCode.PROPENSITY_NOT_FOUND;
import static live.lbtrip.global.error.ErrorCode.TRAVEL_PROFILE_NOT_FOUND;

import org.springframework.http.ResponseEntity;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import live.lbtrip.domain.home.dto.response.HeroResponse;
import live.lbtrip.domain.home.dto.response.HomeFeedResponse;
import live.lbtrip.domain.home.dto.response.HomeIncentiveResponse;
import live.lbtrip.domain.home.dto.response.PopularCourseListResponse;
import live.lbtrip.domain.home.dto.response.ProfileSummaryResponse;
import live.lbtrip.domain.home.dto.response.ProfileTypeListResponse;
import live.lbtrip.global.swagger.ApiErrorCodeResponses;
import live.lbtrip.global.swagger.ApiSuccessResponse;
import live.lbtrip.global.web.UserId;

@Tag(name = "Home", description = "홈 화면 섹션 API")
public interface HomeApi {

    @Operation(
        summary = "홈 히어로 사진",
        description = "비로그인: 랜덤 여행지 사진, 로그인: 내 추천지역 사진."
    )
    @ApiSuccessResponse(description = "히어로 조회 성공")
    @ApiErrorCodeResponses({
        INTERNAL_SERVER_ERROR
    })
    ResponseEntity<HeroResponse> getHero(@UserId(required = false) Long userId);

    @Operation(
        summary = "취향 진단 대표 유형 목록",
        description = "비로그인 홈 히어로에 노출할 대표 취향 진단 유형 목록을 조회합니다."
    )
    @ApiSuccessResponse(description = "대표 유형 목록 조회 성공")
    @ApiErrorCodeResponses({
        INTERNAL_SERVER_ERROR
    })
    ResponseEntity<ProfileTypeListResponse> getProfileTypes();

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "내 진단 요약",
        description = "로그인 사용자의 진단 유형과 랜덤 3개 취향 슬라이더를 조회합니다."
    )
    @ApiSuccessResponse(description = "진단 요약 조회 성공")
    @ApiErrorCodeResponses({
        INVALID_ACCESS_TOKEN,
        PROPENSITY_NOT_FOUND,
        TRAVEL_PROFILE_NOT_FOUND
    })
    ResponseEntity<ProfileSummaryResponse> getProfileSummary(@UserId Long userId);

    @Operation(
        summary = "인기 지역 대표 코스",
        description = "추천 수가 많은 지역의 대표 코스를 조회합니다."
    )
    @ApiSuccessResponse(description = "인기 코스 조회 성공")
    @ApiErrorCodeResponses({
        INTERNAL_SERVER_ERROR
    })
    ResponseEntity<PopularCourseListResponse> getPopularCourses();

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "내 저장 코스 피드",
        description = "저장 코스에 추천 여행지를 인터리빙해 조회합니다."
    )
    @ApiSuccessResponse(description = "저장 코스 피드 조회 성공")
    @ApiErrorCodeResponses({
        INVALID_ACCESS_TOKEN
    })
    ResponseEntity<HomeFeedResponse> getSavedCourseFeed(@UserId Long userId);

    @Operation(
        summary = "진행중 인센티브(지역 탭별)",
        description = "비로그인: 인기 지역, 로그인: 내 추천 지역의 현재 진행중 인센티브를 지역 탭별로 조회합니다."
    )
    @ApiSuccessResponse(description = "진행중 인센티브 조회 성공")
    @ApiErrorCodeResponses({
        INTERNAL_SERVER_ERROR
    })
    ResponseEntity<HomeIncentiveResponse> getIncentives(@UserId(required = false) Long userId);
}
