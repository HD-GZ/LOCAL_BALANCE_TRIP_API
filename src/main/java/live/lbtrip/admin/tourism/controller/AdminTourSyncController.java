package live.lbtrip.admin.tourism.controller;

import static org.springframework.http.HttpStatus.ACCEPTED;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import live.lbtrip.admin.tourism.service.AdminTourSyncService;
import live.lbtrip.global.web.AdminId;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin/tour-sync")
@RequiredArgsConstructor
public class AdminTourSyncController implements AdminTourSyncApi {

    private final AdminTourSyncService adminTourSyncService;

    @PostMapping
    public ResponseEntity<Void> triggerSync(@AdminId Long adminId) {
        adminTourSyncService.triggerSync();
        return ResponseEntity.status(ACCEPTED).build();
    }
}
