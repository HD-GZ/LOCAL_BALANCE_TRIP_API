package live.lbtrip.domain.auth.service;

import java.security.SecureRandom;

import org.springframework.stereotype.Component;

import live.lbtrip.domain.auth.repository.EmailVerificationTokenRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class EmailVerificationCodeGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String VERIFICATION_CODE_FORMAT = "%06d";
    private static final int VERIFICATION_CODE_BOUND = 1_000_000;

    private final EmailVerificationTokenRepository tokenRepository;

    public String generate() {
        String code;
        do {
            code = VERIFICATION_CODE_FORMAT.formatted(RANDOM.nextInt(VERIFICATION_CODE_BOUND));
        } while (tokenRepository.existsByCode(code));
        return code;
    }
}
