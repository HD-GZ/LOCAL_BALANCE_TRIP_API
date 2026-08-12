package live.lbtrip.domain.savedcourse.share.service;

import org.springframework.stereotype.Component;

import live.lbtrip.domain.savedcourse.share.model.entity.CourseShareToken;
import live.lbtrip.domain.savedcourse.share.repository.CourseShareTokenRepository;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CourseShareTokenFinder {

    private final CourseShareTokenRepository courseShareTokenRepository;

    public CourseShareToken findByToken(String token) {
        return courseShareTokenRepository.findByToken(token)
            .orElseThrow(() -> BusinessException.of(ErrorCode.SHARE_TOKEN_NOT_FOUND));
    }
}
