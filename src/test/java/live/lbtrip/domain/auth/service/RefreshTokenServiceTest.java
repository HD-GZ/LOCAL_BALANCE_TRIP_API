package live.lbtrip.domain.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import live.lbtrip.domain.auth.model.RefreshToken;
import live.lbtrip.domain.auth.repository.RefreshTokenRepository;
import live.lbtrip.domain.user.model.Gender;
import live.lbtrip.domain.user.model.User;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    @Test
    void issueReplacesRefreshToken() {
        User user = user();
        when(jwtTokenProvider.createRefreshToken(user)).thenReturn("refresh-token");
        when(jwtTokenProvider.refreshTokenExpiresAt()).thenReturn(LocalDateTime.of(2026, 1, 1, 12, 0));

        String token = refreshTokenService.issue(user);

        assertThat(token).isEqualTo("refresh-token");
        verify(refreshTokenRepository).deleteByUser(user);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void findUsableRejectsNotFoundToken() {
        when(refreshTokenRepository.findByToken("refresh-token")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> refreshTokenService.findUsable(" refresh-token "))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.INVALID_REFRESH_TOKEN);
    }

    @Test
    void findUsableRejectsInvalidJwtToken() {
        RefreshToken refreshToken = RefreshToken.create(
            user(),
            "refresh-token",
            LocalDateTime.now().plusMinutes(10)
        );
        when(refreshTokenRepository.findByToken("refresh-token")).thenReturn(Optional.of(refreshToken));
        when(jwtTokenProvider.isValid("refresh-token")).thenReturn(false);

        assertThatThrownBy(() -> refreshTokenService.findUsable("refresh-token"))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.INVALID_REFRESH_TOKEN);
    }

    @Test
    void findUsableRejectsExpiredToken() {
        RefreshToken refreshToken = RefreshToken.create(
            user(),
            "refresh-token",
            LocalDateTime.now().minusSeconds(1)
        );
        when(refreshTokenRepository.findByToken("refresh-token")).thenReturn(Optional.of(refreshToken));
        when(jwtTokenProvider.isValid("refresh-token")).thenReturn(true);

        assertThatThrownBy(() -> refreshTokenService.findUsable("refresh-token"))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.EXPIRED_REFRESH_TOKEN);
    }

    @Test
    void findUsableReturnsRefreshToken() {
        RefreshToken refreshToken = RefreshToken.create(
            user(),
            "refresh-token",
            LocalDateTime.now().plusMinutes(10)
        );
        when(refreshTokenRepository.findByToken("refresh-token")).thenReturn(Optional.of(refreshToken));
        when(jwtTokenProvider.isValid("refresh-token")).thenReturn(true);

        RefreshToken foundToken = refreshTokenService.findUsable("refresh-token");

        assertThat(foundToken).isSameAs(refreshToken);
    }

    private User user() {
        return User.create(
            "홍길동",
            "user@example.com",
            "encoded-password",
            LocalDate.of(1999, 1, 1),
            Gender.NOT_SPECIFIED,
            true,
            true,
            false
        );
    }
}
