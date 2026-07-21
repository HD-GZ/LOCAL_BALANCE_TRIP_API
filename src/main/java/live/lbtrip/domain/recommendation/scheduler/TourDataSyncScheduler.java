package live.lbtrip.domain.recommendation.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import live.lbtrip.domain.recommendation.service.TourDataSyncService;
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
