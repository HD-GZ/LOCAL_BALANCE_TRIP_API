package live.lbtrip.domain.savedcourse.service;

import org.springframework.stereotype.Component;

import live.lbtrip.domain.savedcourse.repository.SavedCourseRepository;
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
