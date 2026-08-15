package live.lbtrip.domain.user.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import live.lbtrip.domain.user.model.User;
import live.lbtrip.domain.user.model.UserStatus;
import live.lbtrip.domain.user.repository.UserRepository;
import live.lbtrip.global.storage.service.ImageStorage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class UserAnonymizationService {

    private final UserRepository userRepository;
    private final UserAnonymizationProcessor anonymizationProcessor;
    private final ImageStorage imageStorage;
    private final Duration gracePeriod;

    public UserAnonymizationService(
        UserRepository userRepository,
        UserAnonymizationProcessor anonymizationProcessor,
        ImageStorage imageStorage,
        @Value("${app.withdrawal.grace-period}") Duration gracePeriod
    ) {
        this.userRepository = userRepository;
        this.anonymizationProcessor = anonymizationProcessor;
        this.imageStorage = imageStorage;
        this.gracePeriod = gracePeriod;
    }

    public int anonymizeExpiredUsers() {
        List<Long> targetIds = findExpiredUserIds();

        int anonymizedCount = 0;
        for (Long userId : targetIds) {
            List<String> storageKeys;
            try {
                storageKeys = anonymizationProcessor.anonymize(userId);
            } catch (RuntimeException exception) {
                log.error("탈퇴 회원 익명화 실패. userId={}", userId, exception);
                continue;
            }
            anonymizedCount++;
            deleteStorageObjects(storageKeys);
        }
        return anonymizedCount;
    }

    private List<Long> findExpiredUserIds() {
        LocalDateTime cutoff = LocalDateTime.now().minus(gracePeriod);
        return userRepository
            .findAllByStatusAndDeletedAtIsNullAndWithdrawnAtBefore(UserStatus.WITHDRAWN, cutoff)
            .stream()
            .map(User::getId)
            .toList();
    }

    private void deleteStorageObjects(List<String> storageKeys) {
        for (String key : storageKeys) {
            try {
                imageStorage.delete(key);
            } catch (RuntimeException exception) {
                log.warn("탈퇴 회원 이미지 삭제 실패. key={}", key, exception);
            }
        }
    }
}
