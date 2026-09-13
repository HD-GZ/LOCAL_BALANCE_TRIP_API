package live.lbtrip.domain.recommendation.service;

import java.util.Locale;

import org.springframework.stereotype.Component;

import live.lbtrip.domain.recommendation.model.entity.GeneratedCourse;
import live.lbtrip.domain.recommendation.repository.GeneratedCourseRepository;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GeneratedCourseFinder {

    private final GeneratedCourseRepository generatedCourseRepository;

    public GeneratedCourse findByIdAndUserId(Long id, Long userId, Locale locale) {
        return generatedCourseRepository.findByIdAndUserIdAndRecommendedRegionLocale(id, userId, locale)
            .orElseThrow(() -> BusinessException.of(ErrorCode.COURSE_NOT_FOUND));
    }
}
