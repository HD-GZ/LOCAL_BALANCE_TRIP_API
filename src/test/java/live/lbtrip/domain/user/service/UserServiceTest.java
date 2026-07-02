package live.lbtrip.domain.user.service;

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

import live.lbtrip.domain.user.dto.response.EmailAvailabilityResponse;
import live.lbtrip.domain.user.dto.response.UserResponse;
import live.lbtrip.domain.user.model.User;
import live.lbtrip.domain.user.repository.UserRepository;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import live.lbtrip.support.fixture.AuthResponseFixture;
import live.lbtrip.support.fixture.UserFixture;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Nested
    class 이메일_사용_가능_여부 {

        @Test
        void 미사용_이메일이면_true를_응답한다() {
            when(userRepository.existsByEmail(UserFixture.EMAIL)).thenReturn(false);

            EmailAvailabilityResponse response = userService.checkEmailAvailability(UserFixture.EMAIL);

            assertThat(response.available()).isTrue();
        }

        @Test
        void 사용_중인_이메일이면_false를_응답한다() {
            when(userRepository.existsByEmail(UserFixture.EMAIL)).thenReturn(true);

            EmailAvailabilityResponse response = userService.checkEmailAvailability(UserFixture.EMAIL);

            assertThat(response.available()).isFalse();
        }
    }

    @Nested
    class 내_정보_조회 {

        @Test
        void 사용자_정보를_응답한다() {
            User activeUser = UserFixture.activeUser();
            when(userRepository.findById(AuthResponseFixture.USER_ID)).thenReturn(Optional.of(activeUser));

            UserResponse response = userService.getUser(AuthResponseFixture.USER_ID);

            assertThat(response.email()).isEqualTo(UserFixture.EMAIL);
            assertThat(response.name()).isEqualTo(UserFixture.NAME);
            assertThat(response.birthDate()).isEqualTo(UserFixture.BIRTH_DATE);
            assertThat(response.gender()).isEqualTo(UserFixture.GENDER);
            assertThat(response.status()).isEqualTo(activeUser.getStatus());
            assertThat(response.marketingAgreed()).isFalse();
        }

        @Test
        void 사용자가_존재하지_않으면_예외를_던진다() {
            when(userRepository.findById(AuthResponseFixture.USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getUser(AuthResponseFixture.USER_ID))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USER_NOT_FOUND);
        }
    }
}
