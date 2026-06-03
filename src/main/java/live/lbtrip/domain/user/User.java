package live.lbtrip.domain.user;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 50)
	private String name;

	@Column(nullable = false, unique = true, length = 255)
	private String email;

	@Column(nullable = false)
	private String password;

	@Column(nullable = false, length = 20)
	private String phoneNumber;

	@Column(nullable = false)
	private Integer age;

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

	@Column(nullable = false)
	private LocalDateTime createdAt;

	@Column(nullable = false)
	private LocalDateTime updatedAt;

	protected User() {
	}

	private User(
		String name,
		String email,
		String password,
		String phoneNumber,
		Integer age,
		Gender gender,
		boolean termsAgreed,
		boolean privacyAgreed,
		boolean marketingAgreed
	) {
		this.name = name;
		this.email = email;
		this.password = password;
		this.phoneNumber = phoneNumber;
		this.age = age;
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
		String phoneNumber,
		Integer age,
		Gender gender,
		boolean termsAgreed,
		boolean privacyAgreed,
		boolean marketingAgreed
	) {
		return new User(
			name,
			email,
			encodedPassword,
			phoneNumber,
			age,
			gender,
			termsAgreed,
			privacyAgreed,
			marketingAgreed
		);
	}

	@PrePersist
	void prePersist() {
		LocalDateTime now = LocalDateTime.now();
		this.createdAt = now;
		this.updatedAt = now;
	}

	@PreUpdate
	void preUpdate() {
		this.updatedAt = LocalDateTime.now();
	}

	public void verifyEmail() {
		this.status = UserStatus.ACTIVE;
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getEmail() {
		return email;
	}

	public String getPassword() {
		return password;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public Integer getAge() {
		return age;
	}

	public Gender getGender() {
		return gender;
	}

	public UserStatus getStatus() {
		return status;
	}

	public boolean isTermsAgreed() {
		return termsAgreed;
	}

	public boolean isPrivacyAgreed() {
		return privacyAgreed;
	}

	public boolean isMarketingAgreed() {
		return marketingAgreed;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}
}
