package live.lbtrip.domain.savedcourse.share.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import live.lbtrip.domain.savedcourse.share.service.CourseShareService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ShareTokenCleanupScheduler {

    private final CourseShareService courseShareService;

    @Scheduled(cron = "0 10 4 * * *")
    public void deleteExpiredShareTokens() {
        long count = courseShareService.deleteExpiredTokens();
        log.info("만료 공유 토큰 정리 완료. count={}", count);
    }
}
