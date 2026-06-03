package live.lbtrip.domain.auth.model;

import java.time.Instant;

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
import live.lbtrip.global.model.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "refresh_tokens")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken extends BaseEntity {

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

	private RefreshToken(User user, String token, Instant expiresAt) {
		this.user = user;
		this.token = token;
		this.expiresAt = expiresAt;
		this.revoked = false;
	}

	public static RefreshToken create(User user, String token, Instant expiresAt) {
		return new RefreshToken(user, token, expiresAt);
	}

	public boolean isExpired(Instant now) {
		return expiresAt.isBefore(now);
	}

	public void revoke() {
		this.revoked = true;
	}

}
