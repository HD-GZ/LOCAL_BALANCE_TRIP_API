package live.lbtrip.admin.auth.service;

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
import org.springframework.security.crypto.password.PasswordEncoder;

import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import live.lbtrip.support.fixture.AdminFixture;
import live.lbtrip.admin.admin.model.Admin;
import live.lbtrip.admin.admin.repository.AdminRepository;
import live.lbtrip.admin.auth.dto.request.AdminLoginRequest;
import live.lbtrip.admin.auth.dto.response.AdminTokenResponse;
import live.lbtrip.support.fixture.TokenFixture;

@ExtendWith(MockitoExtension.class)
class AdminAuthServiceTest {

    @Mock
    private AdminRepository adminRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AdminJwtTokenProvider adminJwtTokenProvider;

    @InjectMocks
    private AdminAuthService adminAuthService;

    @Nested
    class 로그인 {

        @Test
        void 로그인에_성공하면_어드민_액세스_토큰을_반환한다() {
            Admin admin = AdminFixture.admin();
            when(adminRepository.findByEmail(AdminFixture.EMAIL)).thenReturn(Optional.of(admin));
            when(passwordEncoder.matches(AdminFixture.PASSWORD, AdminFixture.ENCODED_PASSWORD)).thenReturn(true);
            when(adminJwtTokenProvider.createAccessToken(admin)).thenReturn(TokenFixture.ADMIN_ACCESS_TOKEN);

            AdminTokenResponse response = adminAuthService.login(
                new AdminLoginRequest(AdminFixture.EMAIL, AdminFixture.PASSWORD)
            );

            assertThat(response.accessToken()).isEqualTo(TokenFixture.ADMIN_ACCESS_TOKEN);
        }

        @Test
        void 존재하지_않는_이메일이면_예외를_던진다() {
            when(adminRepository.findByEmail(AdminFixture.EMAIL)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> adminAuthService.login(
                new AdminLoginRequest(AdminFixture.EMAIL, AdminFixture.PASSWORD)
            ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_LOGIN_CREDENTIALS);
        }

        @Test
        void 비밀번호가_일치하지_않으면_예외를_던진다() {
            Admin admin = AdminFixture.admin();
            when(adminRepository.findByEmail(AdminFixture.EMAIL)).thenReturn(Optional.of(admin));
            when(passwordEncoder.matches(AdminFixture.PASSWORD, AdminFixture.ENCODED_PASSWORD)).thenReturn(false);

            assertThatThrownBy(() -> adminAuthService.login(
                new AdminLoginRequest(AdminFixture.EMAIL, AdminFixture.PASSWORD)
            ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_LOGIN_CREDENTIALS);
        }
    }
}
