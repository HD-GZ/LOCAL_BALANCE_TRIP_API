package live.lbtrip.domain.user.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import live.lbtrip.global.model.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "이름은 필수입니다.")
    @Size(max = 50, message = "이름은 50자 이하여야 합니다.")
    @Column(nullable = false, length = 50)
    private String name;

    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    @Size(max = 255, message = "이메일은 255자 이하여야 합니다.")
    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(max = 255, message = "비밀번호는 255자 이하여야 합니다.")
    @Column(nullable = false)
    private String password;

    @NotNull(message = "생년월일은 필수입니다.")
    @PastOrPresent(message = "생년월일은 미래 날짜일 수 없습니다.")
    @Column(nullable = false)
    private LocalDate birthDate;

    @NotNull(message = "성별은 필수입니다.")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Gender gender;

    @NotNull(message = "사용자 상태는 필수입니다.")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private UserStatus status;

    @AssertTrue(message = "서비스 이용약관 동의는 필수입니다.")
    @Column(nullable = false)
    private boolean termsAgreed;

    @AssertTrue(message = "개인정보 수집·이용 동의는 필수입니다.")
    @Column(nullable = false)
    private boolean privacyAgreed;

    @Column(nullable = false)
    private boolean marketingAgreed;

    private User(
        String name,
        String email,
        String password,
        LocalDate birthDate,
        Gender gender,
        boolean termsAgreed,
        boolean privacyAgreed,
        boolean marketingAgreed
    ) {
        validateRequiredAgreements(termsAgreed, privacyAgreed);
        this.name = name;
        this.email = email;
        this.password = password;
        this.birthDate = birthDate;
        this.gender = gender;
        this.status = UserStatus.PENDING_EMAIL_VERIFICATION;
        this.termsAgreed = termsAgreed;
        this.privacyAgreed = privacyAgreed;
        this.marketingAgreed = marketingAgreed;
    }

    public static User create(
        String name,
        String email,
        String encodedPassword,
        LocalDate birthDate,
        Gender gender,
        boolean termsAgreed,
        boolean privacyAgreed,
        boolean marketingAgreed
    ) {
        return new User(
            name,
            email,
            encodedPassword,
            birthDate,
            gender,
            termsAgreed,
            privacyAgreed,
            marketingAgreed
        );
    }

    public void verifyEmail() {
        this.status = UserStatus.ACTIVE;
    }

    public void update(String name, LocalDate birthDate, Gender gender) {
        this.name = name;
        this.birthDate = birthDate;
        this.gender = gender;
    }

    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    public boolean isActive() {
        return status == UserStatus.ACTIVE;
    }

    private void validateRequiredAgreements(boolean termsAgreed, boolean privacyAgreed) {
        if (!termsAgreed || !privacyAgreed) {
            throw BusinessException.of(ErrorCode.REQUIRED_AGREEMENT_NOT_ACCEPTED);
        }
    }

}
