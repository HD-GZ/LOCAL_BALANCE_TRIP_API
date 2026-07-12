package live.lbtrip.domain.recommendation.controller;

import static org.springframework.http.HttpStatus.CREATED;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import live.lbtrip.domain.recommendation.dto.response.CourseCandidateResponse;
import live.lbtrip.domain.recommendation.dto.response.CourseDetailResponse;
import live.lbtrip.domain.recommendation.dto.response.CourseDetailResponse.InnerPlaceResponse;
import live.lbtrip.domain.recommendation.dto.response.RegionRecommendationResponse;
import live.lbtrip.global.web.UserId;

@RestController
@RequestMapping("/recommendations")
public class RecommendationController implements RecommendationApi {

    @PostMapping
    public ResponseEntity<Void> createRecommendations(@UserId Long userId) {
        return ResponseEntity.status(CREATED).build();
    }

    @GetMapping("/regions")
    public ResponseEntity<List<RegionRecommendationResponse>> getRecommendedRegions(@UserId Long userId) {
        return ResponseEntity.ok(List.of(
            new RegionRecommendationResponse(1L, "전라북도 임실군",
                "http://tong.visitkorea.or.kr/cms/resource/81/3585581_image2_1.jpg",
                "관광객 발길이 드문 한적한 로컬 분위기 · 전통시장 등 실속 여행 인프라 — 지금 취향과 잘 맞는 지역이에요"),
            new RegionRecommendationResponse(2L, "전라남도 담양군",
                "http://tong.visitkorea.or.kr/cms/resource/76/2846576_image2_1.JPG",
                "로컬 미식 상권이 풍부한 먹거리 · 전통시장 등 실속 여행 인프라 — 지금 취향과 잘 맞는 지역이에요"),
            new RegionRecommendationResponse(3L, "전라북도 정읍시",
                "http://tong.visitkorea.or.kr/cms/resource/92/3366392_image2_1.JPG",
                "관광객 발길이 드문 한적한 로컬 분위기 · 직접 해보는 체험 콘텐츠 — 지금 취향과 잘 맞는 지역이에요"),
            new RegionRecommendationResponse(4L, "전라남도 광양시",
                "http://tong.visitkorea.or.kr/cms/resource/08/3050908_image2_1.jpg",
                "로컬 미식 상권이 풍부한 먹거리 · 전통시장 등 실속 여행 인프라 — 지금 취향과 잘 맞는 지역이에요"),
            new RegionRecommendationResponse(5L, "전라남도 영암군",
                "http://tong.visitkorea.or.kr/cms/resource/33/2678633_image2_1.jpg",
                "관광객 발길이 드문 한적한 로컬 분위기 · 로컬 미식 상권이 풍부한 먹거리 — 지금 취향과 잘 맞는 지역이에요")
        ));
    }

    @GetMapping("/regions/{regionId}/courses")
    public ResponseEntity<List<CourseCandidateResponse>> getRegionCourses(
        @UserId Long userId,
        @PathVariable Long regionId
    ) {
        return ResponseEntity.ok(List.of(
            new CourseCandidateResponse(1L, "전라북도 임실군 골목 미식 코스",
                "http://tong.visitkorea.or.kr/cms/resource/81/3585581_image2_1.jpg",
                "실속 소비 + 로컬 미식 성향을 반영해 노포·골목 상권 위주로 짰어요"),
            new CourseCandidateResponse(2L, "전라북도 임실군 힐링 산책 코스",
                "http://tong.visitkorea.or.kr/cms/resource/78/3536778_image2_1.jpg",
                "느긋한 쉼·자연 몰입 성향을 반영해 저강도 도보 동선으로 짰어요"),
            new CourseCandidateResponse(3L, "전라북도 임실군 생활 체험 코스",
                "http://tong.visitkorea.or.kr/cms/resource/57/3077557_image2_1.jpg",
                "직접 해보기·생활 체험 선호를 담아 시장·공방 중심으로 구성했어요")
        ));
    }

    @GetMapping("/courses/{courseId}")
    public ResponseEntity<CourseDetailResponse> getCourseDetail(
        @UserId Long userId,
        @PathVariable Long courseId
    ) {
        return ResponseEntity.ok(new CourseDetailResponse(
            1L,
            "전라북도 임실군",
            "전라북도 임실군 골목 미식 코스",
            List.of(
                new InnerPlaceResponse(1, "관촌시장",
                    "임실군 관촌면 관촌리에 위치한 관촌시장은 1914년에 개설되어 100년이 넘는 역사를 가진 전통시장이다.",
                    "http://tong.visitkorea.or.kr/cms/resource/81/3585581_image2_1.jpg",
                    127.268474081, 35.6781457432, null, false, null),
                new InnerPlaceResponse(2, "포레드노드",
                    "포레드노드 카페는 전라북도 임실 사선대공원 안에 있는 한적하면서도 분위기 좋은 카페로 유명하다.",
                    "http://tong.visitkorea.or.kr/cms/resource/70/2839270_image2_1.jpg",
                    127.2743765449, 35.6701162017, 16, false, null),
                new InnerPlaceResponse(3, "운서정",
                    "운서정(雲棲亭)은 승지 김양근의 아들 김승희가 부친의 유덕을 추모하기 위하여 1928년에 지은 누정이다.",
                    "http://tong.visitkorea.or.kr/cms/resource/85/3361385_image2_1.JPG",
                    127.2765652376, 35.6701606791, 3, true,
                    "https://sfj608538-sfj608538.ktcdn.co.kr/file/audio/56/16149.mp3"),
                new InnerPlaceResponse(4, "사선대관광지&조각공원",
                    "사선대관광지는 섬진강 상류 오원천 기슭 사선대 주변에 조성되어 1985년 국민관광지로 지정됐다.",
                    "http://tong.visitkorea.or.kr/cms/resource/84/3536784_image2_1.jpg",
                    127.2757222522, 35.6705400302, 2, false, null)
            )
        ));
    }

    @PostMapping("/courses/{courseId}/save")
    public ResponseEntity<Void> saveCourse(
        @UserId Long userId,
        @PathVariable Long courseId
    ) {
        return ResponseEntity.status(CREATED).build();
    }
}
