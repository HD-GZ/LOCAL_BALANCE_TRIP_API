package live.lbtrip.domain.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import live.lbtrip.domain.auth.model.RefreshToken;
import live.lbtrip.domain.auth.repository.RefreshTokenRepository;
import live.lbtrip.domain.user.model.User;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import live.lbtrip.support.fixture.TokenFixture;
import live.lbtrip.support.fixture.UserFixture;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    @Nested
    class 발급 {

        @Test
        void 기존_토큰을_삭제하고_새_토큰을_저장한다() {
            User user = UserFixture.user();
            when(jwtTokenProvider.createRefreshToken(user)).thenReturn(TokenFixture.REFRESH_TOKEN);
            when(jwtTokenProvider.refreshTokenExpiresAt()).thenReturn(LocalDateTime.of(2026, 1, 1, 12, 0));

            String token = refreshTokenService.issue(user);

            assertThat(token).isEqualTo(TokenFixture.REFRESH_TOKEN);
            verify(refreshTokenRepository).deleteByUser(user);
            verify(refreshTokenRepository).save(any(RefreshToken.class));
        }
    }

    @Nested
    class 조회 {

        @Test
        void 토큰을_찾을_수_없으면_예외를_던진다() {
            when(refreshTokenRepository.findByToken(TokenFixture.REFRESH_TOKEN)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> refreshTokenService.findUsable(" " + TokenFixture.REFRESH_TOKEN + " "))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        @Test
        void JWT가_유효하지_않으면_예외를_던진다() {
            RefreshToken refreshToken = usableToken();
            when(refreshTokenRepository.findByToken(TokenFixture.REFRESH_TOKEN)).thenReturn(Optional.of(refreshToken));
            when(jwtTokenProvider.isValid(TokenFixture.REFRESH_TOKEN)).thenReturn(false);

            assertThatThrownBy(() -> refreshTokenService.findUsable(TokenFixture.REFRESH_TOKEN))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        @Test
        void 만료된_토큰이면_예외를_던진다() {
            RefreshToken refreshToken = RefreshToken.create(
                UserFixture.user(),
                TokenFixture.REFRESH_TOKEN,
                LocalDateTime.now().minusSeconds(1)
            );
            when(refreshTokenRepository.findByToken(TokenFixture.REFRESH_TOKEN)).thenReturn(Optional.of(refreshToken));
            when(jwtTokenProvider.isValid(TokenFixture.REFRESH_TOKEN)).thenReturn(true);

            assertThatThrownBy(() -> refreshTokenService.findUsable(TokenFixture.REFRESH_TOKEN))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.EXPIRED_REFRESH_TOKEN);
        }

        @Test
        void 사용_가능한_토큰을_조회한다() {
            RefreshToken refreshToken = usableToken();
            when(refreshTokenRepository.findByToken(TokenFixture.REFRESH_TOKEN)).thenReturn(Optional.of(refreshToken));
            when(jwtTokenProvider.isValid(TokenFixture.REFRESH_TOKEN)).thenReturn(true);

            RefreshToken foundToken = refreshTokenService.findUsable(TokenFixture.REFRESH_TOKEN);

            assertThat(foundToken).isSameAs(refreshToken);
        }
    }

    private RefreshToken usableToken() {
        return RefreshToken.create(
            UserFixture.user(),
            TokenFixture.REFRESH_TOKEN,
            LocalDateTime.now().plusMinutes(10)
        );
    }
}
