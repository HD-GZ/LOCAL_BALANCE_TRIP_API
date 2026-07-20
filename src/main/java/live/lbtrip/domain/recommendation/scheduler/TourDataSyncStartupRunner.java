package live.lbtrip.domain.recommendation.scheduler;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import live.lbtrip.domain.recommendation.service.TourDataSyncService;
import lombok.RequiredArgsConstructor;

@Profile("local")
@Component
@RequiredArgsConstructor
public class TourDataSyncStartupRunner {

    private final TourDataSyncService tourDataSyncService;

    @EventListener(ApplicationReadyEvent.class)
    public void syncOnStartup() {
        tourDataSyncService.syncAll();
    }
}
