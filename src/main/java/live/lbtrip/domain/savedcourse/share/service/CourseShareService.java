package live.lbtrip.domain.savedcourse.share.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import live.lbtrip.domain.incentive.service.IncentiveFinder;
import live.lbtrip.domain.savedcourse.course.service.SavedCourseFinder;
import live.lbtrip.domain.savedcourse.model.entity.SavedCourse;
import live.lbtrip.domain.savedcourse.share.dto.response.ShareTokenResponse;
import live.lbtrip.domain.savedcourse.share.dto.response.SharedCourseDetailResponse;
import live.lbtrip.domain.savedcourse.share.model.entity.CourseShareToken;
import live.lbtrip.domain.savedcourse.share.repository.CourseShareTokenRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseShareService {

    private static final int VALIDITY_DAYS = 7;

    private final SavedCourseFinder savedCourseFinder;
    private final IncentiveFinder incentiveFinder;
    private final CourseShareTokenFinder courseShareTokenFinder;
    private final CourseShareTokenRepository courseShareTokenRepository;

    @Transactional
    public ShareTokenResponse issueShareToken(Long userId, Long savedCourseId) {
        SavedCourse savedCourse = savedCourseFinder.findByIdAndUserId(savedCourseId, userId);
        CourseShareToken shareToken = CourseShareToken.create(
            savedCourse,
            UUID.randomUUID().toString(),
            LocalDateTime.now().plusDays(VALIDITY_DAYS)
        );
        courseShareTokenRepository.save(shareToken);

        return ShareTokenResponse.from(shareToken);
    }

    public SharedCourseDetailResponse getSharedCourseDetail(String token) {
        CourseShareToken shareToken = courseShareTokenFinder.findByToken(token);
        shareToken.validateUsable(LocalDateTime.now());
        SavedCourse savedCourse = shareToken.getSavedCourse();

        return SharedCourseDetailResponse.of(savedCourse, incentiveFinder.findAllByRegion(
            savedCourse.getLdongRegnCd(),
            savedCourse.getLdongSignguCd()
        ));
    }

    @Transactional
    public long deleteExpiredTokens() {
        return courseShareTokenRepository.deleteAllByExpiresAtBefore(LocalDateTime.now());
    }
}
