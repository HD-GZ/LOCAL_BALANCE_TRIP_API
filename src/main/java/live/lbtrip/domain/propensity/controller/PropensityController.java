package live.lbtrip.domain.propensity.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import live.lbtrip.domain.propensity.dto.request.PropensityRequest;
import live.lbtrip.domain.propensity.dto.response.PropensityResponse;
import live.lbtrip.domain.propensity.service.PropensityService;
import live.lbtrip.global.response.ApiResponse;
import live.lbtrip.global.web.UserId;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/propensity")
@RequiredArgsConstructor
public class PropensityController implements PropensityApi {

    private final PropensityService propensityService;

    @PostMapping
    public ResponseEntity<ApiResponse<PropensityResponse>> setPropensity(
        @UserId Long userId,
        @Valid @RequestBody PropensityRequest request
    ) {
        PropensityResponse response = propensityService.setPropensity(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PropensityResponse>> getPropensity(@UserId Long userId) {
        PropensityResponse response = propensityService.getPropensity(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
