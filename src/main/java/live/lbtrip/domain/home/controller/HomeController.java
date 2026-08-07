package live.lbtrip.domain.home.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import live.lbtrip.domain.home.dto.response.ProfileTypeListResponse;
import live.lbtrip.domain.home.service.HomeService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/home")
@RequiredArgsConstructor
public class HomeController implements HomeApi {

    private final HomeService homeService;

    @GetMapping("/profile-types")
    public ResponseEntity<ProfileTypeListResponse> getProfileTypes() {
        return ResponseEntity.ok(homeService.getProfileTypes());
    }
}
