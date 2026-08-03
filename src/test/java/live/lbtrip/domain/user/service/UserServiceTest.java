package live.lbtrip.domain.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import live.lbtrip.domain.auth.service.RefreshTokenService;
import live.lbtrip.domain.user.dto.request.UserUpdateRequest;
import live.lbtrip.domain.user.dto.response.EmailAvailabilityResponse;
import live.lbtrip.domain.user.dto.response.UserResponse;
import live.lbtrip.domain.user.model.User;
import live.lbtrip.domain.user.model.UserStatus;
import live.lbtrip.domain.user.repository.UserRepository;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import live.lbtrip.support.fixture.AuthResponseFixture;
import live.lbtrip.support.fixture.UserFixture;
import live.lbtrip.support.fixture.UserRequestFixture;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RefreshTokenService refreshTokenService;

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

    @Nested
    class 내_정보_수정 {

        @Test
        void 비밀번호_없이_회원_정보를_수정한다() {
            User activeUser = UserFixture.activeUser();
            when(userRepository.findById(AuthResponseFixture.USER_ID)).thenReturn(Optional.of(activeUser));

            UserResponse response = userService.updateUser(AuthResponseFixture.USER_ID, UserRequestFixture.userUpdateRequest());

            assertThat(response.name()).isEqualTo(UserRequestFixture.NEW_NAME);
            assertThat(response.birthDate()).isEqualTo(UserRequestFixture.NEW_BIRTH_DATE);
            assertThat(response.gender()).isEqualTo(UserRequestFixture.NEW_GENDER);
            assertThat(activeUser.getPassword()).isEqualTo(UserFixture.ENCODED_PASSWORD);
        }

        @Test
        void 비밀번호가_있으면_인코딩하여_변경한다() {
            User activeUser = UserFixture.activeUser();
            when(userRepository.findById(AuthResponseFixture.USER_ID)).thenReturn(Optional.of(activeUser));
            when(passwordEncoder.encode(UserRequestFixture.NEW_PASSWORD)).thenReturn(UserRequestFixture.NEW_ENCODED_PASSWORD);

            userService.updateUser(AuthResponseFixture.USER_ID, UserRequestFixture.userUpdateRequestWithPassword());

            assertThat(activeUser.getPassword()).isEqualTo(UserRequestFixture.NEW_ENCODED_PASSWORD);
        }

        @Test
        void 이름의_앞뒤_공백을_제거하고_수정한다() {
            User activeUser = UserFixture.activeUser();
            when(userRepository.findById(AuthResponseFixture.USER_ID)).thenReturn(Optional.of(activeUser));
            UserUpdateRequest request = new UserUpdateRequest(
                "  " + UserRequestFixture.NEW_NAME + "  ",
                UserRequestFixture.NEW_BIRTH_DATE,
                UserRequestFixture.NEW_GENDER,
                null,
                null
            );

            UserResponse response = userService.updateUser(AuthResponseFixture.USER_ID, request);

            assertThat(response.name()).isEqualTo(UserRequestFixture.NEW_NAME);
        }

        @Test
        void 사용자가_존재하지_않으면_예외를_던진다() {
            when(userRepository.findById(AuthResponseFixture.USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.updateUser(AuthResponseFixture.USER_ID, UserRequestFixture.userUpdateRequest()))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USER_NOT_FOUND);
        }
    }

    @Nested
    class 회원탈퇴 {

        @Test
        void 회원을_탈퇴_처리하고_리프레시_토큰을_폐기한다() {
            User user = UserFixture.activeUser();
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));

            userService.withdraw(1L);

            assertThat(user.getStatus()).isEqualTo(UserStatus.WITHDRAWN);
            assertThat(user.getWithdrawnAt()).isNotNull();
            verify(refreshTokenService).deleteByUserId(1L);
        }

        @Test
        void 존재하지_않는_회원이면_예외가_발생한다() {
            when(userRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.withdraw(1L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USER_NOT_FOUND);
        }

        @Test
        void 이미_탈퇴한_회원이면_예외가_발생한다() {
            User user = UserFixture.activeUser();
            user.withdraw(LocalDateTime.now());
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));

            assertThatThrownBy(() -> userService.withdraw(1L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USER_WITHDRAWN);
        }
    }
}
