package live.lbtrip.admin.auth.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import live.lbtrip.admin.admin.model.Admin;
import live.lbtrip.admin.admin.repository.AdminRepository;
import live.lbtrip.admin.auth.dto.request.AdminLoginRequest;
import live.lbtrip.admin.auth.dto.response.AdminTokenResponse;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import live.lbtrip.global.util.StringNormalizer;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminAuthService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminJwtTokenProvider adminJwtTokenProvider;

    public AdminTokenResponse login(AdminLoginRequest request) {
        String email = StringNormalizer.trimToLowerCase(request.email());
        Admin admin = adminRepository.findByEmail(email)
            .orElseThrow(() -> BusinessException.of(ErrorCode.INVALID_LOGIN_CREDENTIALS));

        if (!passwordEncoder.matches(request.password(), admin.getPassword())) {
            throw BusinessException.of(ErrorCode.INVALID_LOGIN_CREDENTIALS);
        }

        return AdminTokenResponse.of(adminJwtTokenProvider.createAccessToken(admin));
    }
}
