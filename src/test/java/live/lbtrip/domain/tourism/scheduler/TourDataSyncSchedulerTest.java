package live.lbtrip.domain.tourism.scheduler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Method;
import java.util.Properties;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;

import live.lbtrip.domain.tourism.service.TourDataSyncService;

@ExtendWith(MockitoExtension.class)
class TourDataSyncSchedulerTest {

    @Mock
    private TourDataSyncService tourDataSyncService;

    @InjectMocks
    private TourDataSyncScheduler tourDataSyncScheduler;

    @Test
    void 매일_오전_7시_30분에_서울_시간으로_전체_적재를_실행한다() throws NoSuchMethodException {
        Method method = TourDataSyncScheduler.class.getDeclaredMethod("syncScheduled");
        Scheduled scheduled = method.getAnnotation(Scheduled.class);
        YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
        yaml.setResources(new ClassPathResource("application.yml"));
        Properties properties = yaml.getObject();

        assertThat(scheduled.cron()).isEqualTo("${tour-api.sync-cron}");
        assertThat(scheduled.zone()).isEqualTo("${tour-api.sync-zone}");
        assertThat(properties).isNotNull();
        assertThat(properties.getProperty("tour-api.sync-cron"))
            .isEqualTo("${TOUR_DATA_SYNC_CRON:0 30 7 * * *}");
        assertThat(properties.getProperty("tour-api.sync-zone"))
            .isEqualTo("${TOUR_DATA_SYNC_ZONE:Asia/Seoul}");

        tourDataSyncScheduler.syncScheduled();

        verify(tourDataSyncService).syncAll();
    }
}
