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
    private int experientiality;

    @Column(nullable = false)
    private int vitality;

    @Column(nullable = false)
    private int sociality;

    @Column(nullable = false)
    private int accommodation;

    @Column(nullable = false)
    private int food;

    @Column(nullable = false)
    private int experience;

    @Column(nullable = false)
    private int transportation;

    @Column(nullable = false)
    private int cafeExhibition;

    private Propensity(User user, PropensityScores scores) {
        this.user = user;
        applyScores(scores);
    }

    public static Propensity create(User user, PropensityScores scores) {
        return new Propensity(user, scores);
    }

    public void updateScores(PropensityScores scores) {
        applyScores(scores);
    }

    private void applyScores(PropensityScores scores) {
        this.locality = scores.locality();
        this.frugality = scores.frugality();
        this.experientiality = scores.experientiality();
        this.vitality = scores.vitality();
        this.sociality = scores.sociality();
        this.accommodation = scores.accommodation();
        this.food = scores.food();
        this.experience = scores.experience();
        this.transportation = scores.transportation();
        this.cafeExhibition = scores.cafeExhibition();
    }
}
