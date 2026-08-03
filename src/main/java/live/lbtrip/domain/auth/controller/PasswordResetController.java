package live.lbtrip.domain.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import live.lbtrip.domain.auth.dto.request.PasswordResetCodeRequest;
import live.lbtrip.domain.auth.dto.request.PasswordResetConfirmRequest;
import live.lbtrip.domain.auth.dto.request.PasswordResetRequest;
import live.lbtrip.domain.auth.dto.response.PasswordResetCodeResponse;
import live.lbtrip.domain.auth.dto.response.PasswordResetTokenResponse;
import live.lbtrip.domain.auth.service.PasswordResetService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth/password-reset")
@RequiredArgsConstructor
public class PasswordResetController implements PasswordResetApi {

    private final PasswordResetService passwordResetService;

    @PostMapping("/request")
    public ResponseEntity<PasswordResetCodeResponse> requestPasswordReset(
        @Valid @RequestBody PasswordResetCodeRequest request
    ) {
        PasswordResetCodeResponse response = passwordResetService.request(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/confirm")
    public ResponseEntity<PasswordResetTokenResponse> confirmPasswordReset(
        @Valid @RequestBody PasswordResetConfirmRequest request
    ) {
        PasswordResetTokenResponse response = passwordResetService.confirm(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody PasswordResetRequest request) {
        passwordResetService.reset(request);
        return ResponseEntity.ok().build();
    }
}
