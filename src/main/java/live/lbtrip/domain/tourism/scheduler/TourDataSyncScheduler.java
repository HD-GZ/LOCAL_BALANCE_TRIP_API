package live.lbtrip.domain.tourism.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import live.lbtrip.domain.tourism.service.TourDataSyncService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TourDataSyncScheduler {

    private final TourDataSyncService tourDataSyncService;

    // @Scheduled(cron = "0 0 4 * * *")
    public void syncScheduled() {
        tourDataSyncService.syncAll();
    }
}
