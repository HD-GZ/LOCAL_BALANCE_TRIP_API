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
@Table(name = "password_reset_tokens")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PasswordResetToken extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 6)
    private String code;

    @Column(nullable = false)
    private LocalDateTime codeExpiresAt;

    @Column(unique = true, length = 36)
    private String resetToken;

    private LocalDateTime tokenExpiresAt;

    @Column(nullable = false)
    private boolean used;

    private PasswordResetToken(User user, String code, LocalDateTime codeExpiresAt) {
        this.user = user;
        this.code = code;
        this.codeExpiresAt = codeExpiresAt;
        this.used = false;
    }

    public static PasswordResetToken create(User user, String code, LocalDateTime codeExpiresAt) {
        return new PasswordResetToken(user, code, codeExpiresAt);
    }

    public void issueResetToken(String resetToken, LocalDateTime now, LocalDateTime tokenExpiresAt) {
        if (this.resetToken != null) {
            throw BusinessException.of(ErrorCode.PASSWORD_RESET_CODE_USED);
        }
        if (codeExpiresAt.isBefore(now)) {
            throw BusinessException.of(ErrorCode.PASSWORD_RESET_CODE_EXPIRED);
        }
        this.resetToken = resetToken;
        this.tokenExpiresAt = tokenExpiresAt;
    }

    public void use(LocalDateTime now) {
        if (used) {
            throw BusinessException.of(ErrorCode.PASSWORD_RESET_TOKEN_USED);
        }
        if (tokenExpiresAt.isBefore(now)) {
            throw BusinessException.of(ErrorCode.PASSWORD_RESET_TOKEN_EXPIRED);
        }
        this.used = true;
    }
}
