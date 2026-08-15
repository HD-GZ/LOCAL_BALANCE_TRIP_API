package live.lbtrip.admin.tourism.service;

import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import live.lbtrip.domain.tourism.model.enums.TourSyncStep;
import live.lbtrip.domain.tourism.service.TourDataSyncService;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminTourSyncService {

    private final TourDataSyncService tourDataSyncService;
    private final TaskExecutor applicationTaskExecutor;

    private final AtomicBoolean running = new AtomicBoolean(false);

    public void triggerSync() {
        run(tourDataSyncService::syncAll, "전체");
    }

    public void triggerSync(TourSyncStep step) {
        run(() -> tourDataSyncService.sync(step), step.name());
    }

    private void run(Runnable task, String label) {
        if (!running.compareAndSet(false, true)) {
            throw BusinessException.of(ErrorCode.TOUR_SYNC_IN_PROGRESS);
        }
        applicationTaskExecutor.execute(() -> {
            try {
                task.run();
            } catch (Exception e) {
                log.error("어드민 트리거 관광 데이터 동기화 실패: step={}", label, e);
            } finally {
                running.set(false);
            }
        });
    }
}
