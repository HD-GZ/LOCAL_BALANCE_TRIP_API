package live.lbtrip.domain.incentive.controller;

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
import live.lbtrip.domain.incentive.dto.request.IncentiveRequest;
import live.lbtrip.domain.incentive.dto.response.IncentiveResponse;
import live.lbtrip.domain.incentive.service.IncentiveService;
import live.lbtrip.global.web.AdminId;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin/incentives")
@RequiredArgsConstructor
public class IncentiveController implements IncentiveApi {

    private final IncentiveService incentiveService;

    @PostMapping
    public ResponseEntity<IncentiveResponse> createIncentive(
        @AdminId Long adminId,
        @Valid @RequestBody IncentiveRequest request
    ) {
        IncentiveResponse response = incentiveService.createIncentive(request);
        return ResponseEntity.status(CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<IncentiveResponse>> getIncentives(@AdminId Long adminId) {
        List<IncentiveResponse> response = incentiveService.getIncentives();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{incentiveId}")
    public ResponseEntity<IncentiveResponse> updateIncentive(
        @AdminId Long adminId,
        @PathVariable Long incentiveId,
        @Valid @RequestBody IncentiveRequest request
    ) {
        IncentiveResponse response = incentiveService.updateIncentive(incentiveId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{incentiveId}")
    public ResponseEntity<Void> deleteIncentive(
        @AdminId Long adminId,
        @PathVariable Long incentiveId
    ) {
        incentiveService.deleteIncentive(incentiveId);
        return ResponseEntity.ok().build();
    }
}
