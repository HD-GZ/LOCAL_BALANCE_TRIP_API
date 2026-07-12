package live.lbtrip.domain.admin.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import live.lbtrip.domain.admin.auth.dto.request.AdminLoginRequest;
import live.lbtrip.domain.admin.auth.dto.response.AdminTokenResponse;
import live.lbtrip.domain.admin.auth.service.AdminAuthService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin/auth")
@RequiredArgsConstructor
public class AdminAuthController implements AdminAuthApi {

    private final AdminAuthService adminAuthService;

    @PostMapping("/login")
    public ResponseEntity<AdminTokenResponse> login(@Valid @RequestBody AdminLoginRequest request) {
        AdminTokenResponse response = adminAuthService.login(request);
        return ResponseEntity.ok(response);
    }
}
