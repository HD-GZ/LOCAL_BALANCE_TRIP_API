package live.lbtrip.domain.recommendation.service;

import org.springframework.stereotype.Component;

import live.lbtrip.domain.recommendation.repository.SavedCourseRepository;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SaveCourseValidator {

    private final SavedCourseRepository savedCourseRepository;

    public void validateNew(Long userId, Long courseId) {
        if (savedCourseRepository.existsByUserIdAndSourceCourseId(userId, courseId)) {
            throw BusinessException.of(ErrorCode.DUPLICATE_SAVE_COURSE);
        }
    }
}
