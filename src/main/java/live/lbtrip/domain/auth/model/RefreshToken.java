package live.lbtrip.domain.auth.model;

import java.time.Instant;
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
@Table(name = "refresh_tokens")
public class RefreshToken {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(nullable = false, unique = true, length = 500)
	private String token;

	@Column(nullable = false)
	private Instant expiresAt;

	@Column(nullable = false)
	private boolean revoked;

	@Column(nullable = false)
	private LocalDateTime createdAt;

	protected RefreshToken() {
	}

	private RefreshToken(User user, String token, Instant expiresAt) {
		this.user = user;
		this.token = token;
		this.expiresAt = expiresAt;
		this.revoked = false;
	}

	public static RefreshToken create(User user, String token, Instant expiresAt) {
		return new RefreshToken(user, token, expiresAt);
	}

	@PrePersist
	void prePersist() {
		this.createdAt = LocalDateTime.now();
	}

	public boolean isExpired(Instant now) {
		return expiresAt.isBefore(now);
	}

	public void revoke() {
		this.revoked = true;
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

	public Instant getExpiresAt() {
		return expiresAt;
	}

	public boolean isRevoked() {
		return revoked;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}
}
