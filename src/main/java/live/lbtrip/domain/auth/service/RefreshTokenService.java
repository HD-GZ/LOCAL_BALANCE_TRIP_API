package live.lbtrip.domain.auth.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import live.lbtrip.domain.auth.model.RefreshToken;
import live.lbtrip.domain.auth.repository.RefreshTokenRepository;
import live.lbtrip.domain.user.model.User;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import live.lbtrip.global.util.StringNormalizer;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public String issue(User user) {
        RefreshToken refreshToken = RefreshToken.create(
            user,
            jwtTokenProvider.createRefreshToken(user),
            jwtTokenProvider.refreshTokenExpiresAt()
        );

        refreshTokenRepository.deleteByUser(user);
        refreshTokenRepository.save(refreshToken);

        return refreshToken.getToken();
    }

    public RefreshToken findUsable(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(StringNormalizer.trim(token))
            .orElseThrow(() -> BusinessException.of(ErrorCode.INVALID_REFRESH_TOKEN));

        if (!jwtTokenProvider.isValid(refreshToken.getToken())) {
            throw BusinessException.of(ErrorCode.INVALID_REFRESH_TOKEN);
        }
        refreshToken.validateNotExpired(LocalDateTime.now());

        return refreshToken;
    }

    @Transactional
    public void deleteByUserId(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }
}
