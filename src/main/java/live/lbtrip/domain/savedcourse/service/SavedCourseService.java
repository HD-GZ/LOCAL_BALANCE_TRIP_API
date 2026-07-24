package live.lbtrip.domain.savedcourse.service;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import live.lbtrip.domain.incentive.service.IncentiveFinder;
import live.lbtrip.domain.recommendation.model.entity.GeneratedCourse;
import live.lbtrip.domain.recommendation.service.GeneratedCourseFinder;
import live.lbtrip.domain.savedcourse.dto.response.SavedCourseDetailResponse;
import live.lbtrip.domain.savedcourse.dto.response.SavedCourseListResponse;
import live.lbtrip.domain.savedcourse.model.entity.SavedCourse;
import live.lbtrip.domain.savedcourse.repository.SavedCourseRepository;
import live.lbtrip.domain.user.model.User;
import live.lbtrip.domain.user.service.UserFinder;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import live.lbtrip.global.web.PageQueryRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SavedCourseService {

    private final SavedCourseRepository savedCourseRepository;
    private final IncentiveFinder incentiveFinder;
    private final SaveCourseManager saveCourseManager;
    private final SaveCourseValidator saveCourseValidator;
    private final GeneratedCourseFinder generatedCourseFinder;
    private final UserFinder userFinder;

    @Transactional
    public void saveCourse(Long userId, Long courseId) {
        saveCourseValidator.validateNew(userId, courseId);
        GeneratedCourse course = generatedCourseFinder.findByIdAndUserId(courseId, userId);
        User user = userFinder.findById(userId);
        saveCourseManager.add(course, user);
    }

    public SavedCourseListResponse getSavedCourses(Long userId, PageQueryRequest pageQuery) {
        Page<SavedCourse> savedCourses = savedCourseRepository.findAllByUserIdOrderByIdDesc(
            userId, pageQuery.toPageRequest());

        return SavedCourseListResponse.from(savedCourses);
    }

    public SavedCourseDetailResponse getSavedCourseDetail(Long userId, Long savedCourseId) {
        SavedCourse savedCourse = savedCourseRepository.findByIdAndUserId(savedCourseId, userId)
            .orElseThrow(() -> BusinessException.of(ErrorCode.SAVED_COURSE_NOT_FOUND));

        return SavedCourseDetailResponse.of(savedCourse, incentiveFinder.findAllByRegion(
            savedCourse.getLdongRegnCd(),
            savedCourse.getLdongSignguCd()
        ));
    }
}
