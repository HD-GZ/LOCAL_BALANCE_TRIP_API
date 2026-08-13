package live.lbtrip.admin.tourism.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import live.lbtrip.domain.tourism.model.enums.TourSyncStep;
import live.lbtrip.domain.tourism.service.TourDataSyncService;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;

@ExtendWith(MockitoExtension.class)
class AdminTourSyncServiceTest {

    @Mock
    private TourDataSyncService tourDataSyncService;

    @Test
    void 동기화를_백그라운드_작업으로_실행한다() {
        AdminTourSyncService service = new AdminTourSyncService(tourDataSyncService, Runnable::run);

        service.triggerSync();

        verify(tourDataSyncService).syncAll();
    }

    @Test
    void 실행_중에_다시_요청하면_예외를_던진다() {
        List<Runnable> deferred = new ArrayList<>();
        AdminTourSyncService service = new AdminTourSyncService(tourDataSyncService, deferred::add);

        service.triggerSync();

        assertThatThrownBy(service::triggerSync)
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.TOUR_SYNC_IN_PROGRESS);
    }

    @Test
    void 동기화가_끝나면_다시_실행할_수_있다() {
        List<Runnable> deferred = new ArrayList<>();
        AdminTourSyncService service = new AdminTourSyncService(tourDataSyncService, deferred::add);

        service.triggerSync();
        deferred.getFirst().run();

        service.triggerSync();
        assertThat(deferred).hasSize(2);
    }

    @Test
    void 지정한_단계만_실행한다() {
        AdminTourSyncService service = new AdminTourSyncService(tourDataSyncService, Runnable::run);

        service.triggerSync(TourSyncStep.OVERVIEWS);

        verify(tourDataSyncService).sync(TourSyncStep.OVERVIEWS);
        verify(tourDataSyncService, never()).syncAll();
    }

    @Test
    void 단계_실행_중에는_전체_실행도_거부한다() {
        List<Runnable> deferred = new ArrayList<>();
        AdminTourSyncService service = new AdminTourSyncService(tourDataSyncService, deferred::add);

        service.triggerSync(TourSyncStep.OVERVIEWS);

        assertThatThrownBy(service::triggerSync)
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.TOUR_SYNC_IN_PROGRESS);
    }

    @Test
    void 동기화가_실패해도_실행_상태가_풀린다() {
        doThrow(new RuntimeException("sync failed")).when(tourDataSyncService).syncAll();
        AdminTourSyncService service = new AdminTourSyncService(tourDataSyncService, Runnable::run);

        service.triggerSync();

        service.triggerSync();
        verify(tourDataSyncService, times(2)).syncAll();
    }
}
