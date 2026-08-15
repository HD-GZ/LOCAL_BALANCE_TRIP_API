package live.lbtrip.domain.savedcourse.share.model.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import live.lbtrip.domain.savedcourse.model.entity.SavedCourse;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import live.lbtrip.support.fixture.CourseShareFixture;

class CourseShareTokenTest {

    @Nested
    class 생성 {

        @Test
        void 코스와_토큰과_만료시각으로_생성한다() {
            SavedCourse savedCourse = CourseShareFixture.savedCourse();
            LocalDateTime expiresAt = LocalDateTime.of(2026, 8, 19, 13, 0);

            CourseShareToken shareToken = CourseShareToken.create(
                savedCourse, CourseShareFixture.TOKEN, expiresAt);

            assertThat(shareToken.getSavedCourse()).isEqualTo(savedCourse);
            assertThat(shareToken.getToken()).isEqualTo(CourseShareFixture.TOKEN);
            assertThat(shareToken.getExpiresAt()).isEqualTo(expiresAt);
        }
    }

    @Nested
    class 사용_가능_검증 {

        @Test
        void 만료_전이면_통과한다() {
            CourseShareToken shareToken = CourseShareFixture.shareToken();

            assertThatCode(() -> shareToken.validateUsable(CourseShareFixture.EXPIRES_AT.minusSeconds(1)))
                .doesNotThrowAnyException();
        }

        @Test
        void 만료_후면_예외를_던진다() {
            CourseShareToken shareToken = CourseShareFixture.shareToken();

            assertThatThrownBy(() -> shareToken.validateUsable(CourseShareFixture.EXPIRES_AT.plusSeconds(1)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.SHARE_TOKEN_EXPIRED);
        }
    }
}
