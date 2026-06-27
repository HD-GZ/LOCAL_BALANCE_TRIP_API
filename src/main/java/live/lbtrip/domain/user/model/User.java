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

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private UserStatus status;

    @Column(nullable = false)
    private boolean termsAgreed;

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

}
