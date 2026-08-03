package live.lbtrip.domain.auth.service;

import java.security.SecureRandom;

import org.springframework.stereotype.Component;

@Component
public class PasswordResetCodeGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String CODE_FORMAT = "%06d";
    private static final int CODE_BOUND = 1_000_000;

    public String generate() {
        return CODE_FORMAT.formatted(RANDOM.nextInt(CODE_BOUND));
    }
}
