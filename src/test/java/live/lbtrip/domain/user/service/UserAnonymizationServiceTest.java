package live.lbtrip.domain.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import live.lbtrip.domain.user.model.User;
import live.lbtrip.domain.user.model.UserStatus;
import live.lbtrip.domain.user.repository.UserRepository;
import live.lbtrip.global.storage.service.ImageStorage;
import live.lbtrip.support.fixture.UserFixture;

@ExtendWith(MockitoExtension.class)
class UserAnonymizationServiceTest {

    private static final Duration GRACE_PERIOD = Duration.ofDays(30);

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserAnonymizationProcessor anonymizationProcessor;

    @Mock
    private ImageStorage imageStorage;

    private UserAnonymizationService anonymizationService;

    @BeforeEach
    void setUp() {
        anonymizationService = new UserAnonymizationService(
            userRepository,
            anonymizationProcessor,
            imageStorage,
            GRACE_PERIOD
        );
    }

    @Nested
    class 만료_회원_익명화 {

        @Test
        void 유예기간이_지난_탈퇴_회원을_익명화하고_이미지를_삭제한다() {
            User user = UserFixture.withdrawnUser(1L, LocalDateTime.now().minusDays(40));
            when(userRepository.findAllByStatusAndDeletedAtIsNullAndWithdrawnAtBefore(
                any(UserStatus.class), any(LocalDateTime.class)))
                .thenReturn(List.of(user));
            when(anonymizationProcessor.anonymize(1L)).thenReturn(List.of("images/1.jpg"));

            int count = anonymizationService.anonymizeExpiredUsers();

            assertThat(count).isEqualTo(1);
            verify(anonymizationProcessor).anonymize(1L);
            verify(imageStorage).delete("images/1.jpg");
        }

        @Test
        void 대상이_없으면_아무것도_처리하지_않는다() {
            when(userRepository.findAllByStatusAndDeletedAtIsNullAndWithdrawnAtBefore(
                any(UserStatus.class), any(LocalDateTime.class)))
                .thenReturn(List.of());

            int count = anonymizationService.anonymizeExpiredUsers();

            assertThat(count).isZero();
            verifyNoInteractions(anonymizationProcessor);
            verifyNoInteractions(imageStorage);
        }

        @Test
        void 한_회원의_익명화가_실패해도_나머지_회원을_계속_처리한다() {
            User failing = UserFixture.withdrawnUser(1L, LocalDateTime.now().minusDays(40));
            User succeeding = UserFixture.withdrawnUser(2L, LocalDateTime.now().minusDays(40));
            when(userRepository.findAllByStatusAndDeletedAtIsNullAndWithdrawnAtBefore(
                any(UserStatus.class), any(LocalDateTime.class)))
                .thenReturn(List.of(failing, succeeding));
            when(anonymizationProcessor.anonymize(1L)).thenThrow(new IllegalStateException("익명화 실패"));
            when(anonymizationProcessor.anonymize(2L)).thenReturn(List.of("images/2.jpg"));

            int count = anonymizationService.anonymizeExpiredUsers();

            assertThat(count).isEqualTo(1);
            verify(anonymizationProcessor).anonymize(2L);
            verify(imageStorage).delete("images/2.jpg");
            verify(imageStorage, never()).delete("images/1.jpg");
        }

        @Test
        void 이미지_삭제가_실패해도_익명화_건수에_포함된다() {
            User user = UserFixture.withdrawnUser(1L, LocalDateTime.now().minusDays(40));
            when(userRepository.findAllByStatusAndDeletedAtIsNullAndWithdrawnAtBefore(
                any(UserStatus.class), any(LocalDateTime.class)))
                .thenReturn(List.of(user));
            when(anonymizationProcessor.anonymize(1L)).thenReturn(List.of("images/1.jpg"));
            doThrow(new IllegalStateException("삭제 실패")).when(imageStorage).delete(anyString());

            int count = anonymizationService.anonymizeExpiredUsers();

            assertThat(count).isEqualTo(1);
            verify(imageStorage).delete("images/1.jpg");
        }
    }
}
