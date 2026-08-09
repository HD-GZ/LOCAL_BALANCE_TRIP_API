package live.lbtrip.domain.home.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import live.lbtrip.domain.home.dto.response.HeroResponse;
import live.lbtrip.domain.home.dto.response.HomeFeedResponse;
import live.lbtrip.domain.home.dto.response.HomeIncentiveResponse;
import live.lbtrip.domain.home.dto.response.PopularCourseListResponse;
import live.lbtrip.domain.home.dto.response.ProfileSummaryResponse;
import live.lbtrip.domain.home.dto.response.ProfileTypeListResponse;
import live.lbtrip.domain.home.service.HomeService;
import live.lbtrip.global.web.UserId;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/home")
@RequiredArgsConstructor
public class HomeController implements HomeApi {

    private final HomeService homeService;

    @GetMapping("/hero")
    public ResponseEntity<HeroResponse> getHero(@UserId(required = false) Long userId) {
        return ResponseEntity.ok(homeService.getHero(userId));
    }

    @GetMapping("/profile-types")
    public ResponseEntity<ProfileTypeListResponse> getProfileTypes() {
        return ResponseEntity.ok(homeService.getProfileTypes());
    }

    @GetMapping("/profile-summary")
    public ResponseEntity<ProfileSummaryResponse> getProfileSummary(@UserId Long userId) {
        return ResponseEntity.ok(homeService.getProfileSummary(userId));
    }

    @GetMapping("/popular-courses")
    public ResponseEntity<PopularCourseListResponse> getPopularCourses() {
        return ResponseEntity.ok(homeService.getPopularCourses());
    }

    @GetMapping("/saved-courses")
    public ResponseEntity<HomeFeedResponse> getSavedCourseFeed(@UserId Long userId) {
        return ResponseEntity.ok(homeService.getSavedCourseFeed(userId));
    }

    @GetMapping("/incentives")
    public ResponseEntity<HomeIncentiveResponse> getIncentives(@UserId(required = false) Long userId) {
        return ResponseEntity.ok(homeService.getIncentives(userId));
    }
}
