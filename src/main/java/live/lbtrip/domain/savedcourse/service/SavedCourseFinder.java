package live.lbtrip.domain.savedcourse.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import live.lbtrip.domain.savedcourse.model.enums.SavedCourseStatus;
import live.lbtrip.domain.savedcourse.model.entity.SavedCourse;
import live.lbtrip.domain.savedcourse.repository.SavedCourseRepository;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SavedCourseFinder {

    private final SavedCourseRepository savedCourseRepository;

    public Page<SavedCourse> findAllByUserId(Long userId, SavedCourseStatus status, Pageable pageable) {
        if (status == null) {
            return savedCourseRepository.findAllByUserIdOrderByIdDesc(userId, pageable);
        }
        return savedCourseRepository.findAllByUserIdAndStatusOrderByIdDesc(userId, status, pageable);
    }

    public SavedCourse findByIdAndUserId(Long id, Long userId) {
        return savedCourseRepository.findByIdAndUserId(id, userId)
            .orElseThrow(() -> BusinessException.of(ErrorCode.SAVED_COURSE_NOT_FOUND));
    }
}
