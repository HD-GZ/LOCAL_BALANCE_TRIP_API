package live.lbtrip.domain.savedcourse.share.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import live.lbtrip.domain.savedcourse.share.model.entity.CourseShareToken;
import live.lbtrip.domain.savedcourse.share.repository.CourseShareTokenRepository;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import live.lbtrip.support.fixture.CourseShareFixture;

@ExtendWith(MockitoExtension.class)
class CourseShareTokenFinderTest {

    @Mock
    private CourseShareTokenRepository courseShareTokenRepository;

    @InjectMocks
    private CourseShareTokenFinder courseShareTokenFinder;

    @Nested
    class 토큰_조회 {

        @Test
        void 토큰_문자열로_조회한다() {
            CourseShareToken shareToken = CourseShareFixture.shareToken();
            when(courseShareTokenRepository.findByToken(CourseShareFixture.TOKEN))
                .thenReturn(Optional.of(shareToken));

            CourseShareToken result = courseShareTokenFinder.findByToken(CourseShareFixture.TOKEN);

            assertThat(result).isEqualTo(shareToken);
        }

        @Test
        void 토큰이_없으면_예외를_던진다() {
            when(courseShareTokenRepository.findByToken(CourseShareFixture.TOKEN))
                .thenReturn(Optional.empty());

            assertThatThrownBy(() -> courseShareTokenFinder.findByToken(CourseShareFixture.TOKEN))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.SHARE_TOKEN_NOT_FOUND);
        }
    }
}
