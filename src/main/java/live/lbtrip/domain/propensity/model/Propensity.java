package live.lbtrip.domain.propensity.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import live.lbtrip.domain.user.model.User;
import live.lbtrip.global.model.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "propensities")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Propensity extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false, unique = true)
	private User user;

	@Column(nullable = false)
	private int locality;

	@Column(nullable = false)
	private int frugality;

	@Column(nullable = false)
	private int flexibility;

	@Column(nullable = false)
	private int experientiality;

	@Column(nullable = false)
	private int vitality;

	@Column(nullable = false)
	private int sociality;

	private Propensity(
		User user,
		int locality,
		int frugality,
		int flexibility,
		int experientiality,
		int vitality,
		int sociality
	) {
		this.user = user;
		this.locality = locality;
		this.frugality = frugality;
		this.flexibility = flexibility;
		this.experientiality = experientiality;
		this.vitality = vitality;
		this.sociality = sociality;
	}

	public static Propensity create(
		User user,
		int locality,
		int frugality,
		int flexibility,
		int experientiality,
		int vitality,
		int sociality
	) {
		return new Propensity(user, locality, frugality, flexibility, experientiality, vitality, sociality);
	}

	public void updateScores(
		int locality,
		int frugality,
		int flexibility,
		int experientiality,
		int vitality,
		int sociality
	) {
		this.locality = locality;
		this.frugality = frugality;
		this.flexibility = flexibility;
		this.experientiality = experientiality;
		this.vitality = vitality;
		this.sociality = sociality;
	}
}
