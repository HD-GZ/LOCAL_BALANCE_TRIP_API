package live.lbtrip.domain.user.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import live.lbtrip.domain.user.service.UserAnonymizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserWithdrawalScheduler {

    private final UserAnonymizationService anonymizationService;

    @Scheduled(cron = "0 0 4 * * *")
    public void anonymizeExpiredUsers() {
        int count = anonymizationService.anonymizeExpiredUsers();
        log.info("탈퇴 유예기간 만료 회원 익명화 완료. count={}", count);
    }
}
