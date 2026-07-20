package live.lbtrip.domain.recommendation.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TourDataSyncScheduler {

    private final TourDataSyncService tourDataSyncService;

    @Scheduled(cron = "0 0 4 * * *")
    public void syncScheduled() {
        tourDataSyncService.syncAll();
    }
}
