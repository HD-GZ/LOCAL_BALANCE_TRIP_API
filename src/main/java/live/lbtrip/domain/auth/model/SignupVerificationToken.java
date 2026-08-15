package live.lbtrip.domain.auth.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import live.lbtrip.domain.user.model.User;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import live.lbtrip.global.model.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "signup_verification_tokens")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SignupVerificationToken extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "token", nullable = false, unique = true, length = 6)
    private String code;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private boolean used;

    private SignupVerificationToken(User user, String code, LocalDateTime expiresAt) {
        this.user = user;
        this.code = code;
        this.expiresAt = expiresAt;
        this.used = false;
    }

    public static SignupVerificationToken create(User user, String code, LocalDateTime expiresAt) {
        return new SignupVerificationToken(user, code, expiresAt);
    }

    private boolean isExpired(LocalDateTime now) {
        return expiresAt.isBefore(now);
    }

    public void use(LocalDateTime now) {
        if (used) {
            throw BusinessException.of(ErrorCode.EMAIL_VERIFICATION_CODE_USED);
        }
        if (isExpired(now)) {
            throw BusinessException.of(ErrorCode.EMAIL_VERIFICATION_CODE_EXPIRED);
        }

        this.used = true;
    }

}
