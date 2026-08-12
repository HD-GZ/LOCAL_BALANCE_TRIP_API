package live.lbtrip.admin.tourism.service;

import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

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
        if (!running.compareAndSet(false, true)) {
            throw BusinessException.of(ErrorCode.TOUR_SYNC_IN_PROGRESS);
        }
        applicationTaskExecutor.execute(() -> {
            try {
                tourDataSyncService.syncAll();
            } catch (Exception e) {
                log.error("어드민 트리거 관광 데이터 동기화 실패", e);
            } finally {
                running.set(false);
            }
        });
    }
}
