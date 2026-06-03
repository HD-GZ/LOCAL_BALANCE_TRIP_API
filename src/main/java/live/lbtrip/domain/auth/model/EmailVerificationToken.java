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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import live.lbtrip.domain.user.model.User;

@Entity
@Table(name = "email_verification_tokens")
public class EmailVerificationToken {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(nullable = false, unique = true, length = 100)
	private String token;

	@Column(nullable = false)
	private LocalDateTime expiresAt;

	@Column(nullable = false)
	private boolean used;

	@Column(nullable = false)
	private LocalDateTime createdAt;

	protected EmailVerificationToken() {
	}

	private EmailVerificationToken(User user, String token, LocalDateTime expiresAt) {
		this.user = user;
		this.token = token;
		this.expiresAt = expiresAt;
		this.used = false;
	}

	public static EmailVerificationToken create(User user, String token, LocalDateTime expiresAt) {
		return new EmailVerificationToken(user, token, expiresAt);
	}

	@PrePersist
	void prePersist() {
		this.createdAt = LocalDateTime.now();
	}

	public boolean isExpired(LocalDateTime now) {
		return expiresAt.isBefore(now);
	}

	public void use() {
		this.used = true;
	}

	public Long getId() {
		return id;
	}

	public User getUser() {
		return user;
	}

	public String getToken() {
		return token;
	}

	public LocalDateTime getExpiresAt() {
		return expiresAt;
	}

	public boolean isUsed() {
		return used;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}
}
