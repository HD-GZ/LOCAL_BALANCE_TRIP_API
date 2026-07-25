package live.lbtrip.admin.incentive.controller;

import static org.springframework.http.HttpStatus.CREATED;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import live.lbtrip.admin.incentive.dto.request.AdminIncentiveRequest;
import live.lbtrip.admin.incentive.dto.response.AdminIncentiveResponse;
import live.lbtrip.admin.incentive.service.AdminIncentiveService;
import live.lbtrip.global.web.AdminId;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin/incentives")
@RequiredArgsConstructor
public class AdminIncentiveController implements AdminIncentiveApi {

    private final AdminIncentiveService adminIncentiveService;

    @PostMapping
    public ResponseEntity<AdminIncentiveResponse> createIncentive(
        @AdminId Long adminId,
        @Valid @RequestBody AdminIncentiveRequest request
    ) {
        AdminIncentiveResponse response = adminIncentiveService.createIncentive(request);
        return ResponseEntity.status(CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<AdminIncentiveResponse>> getIncentives(@AdminId Long adminId) {
        List<AdminIncentiveResponse> response = adminIncentiveService.getIncentives();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{incentiveId}")
    public ResponseEntity<AdminIncentiveResponse> updateIncentive(
        @AdminId Long adminId,
        @PathVariable Long incentiveId,
        @Valid @RequestBody AdminIncentiveRequest request
    ) {
        AdminIncentiveResponse response = adminIncentiveService.updateIncentive(incentiveId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{incentiveId}")
    public ResponseEntity<Void> deleteIncentive(
        @AdminId Long adminId,
        @PathVariable Long incentiveId
    ) {
        adminIncentiveService.deleteIncentive(incentiveId);
        return ResponseEntity.ok().build();
    }
}
