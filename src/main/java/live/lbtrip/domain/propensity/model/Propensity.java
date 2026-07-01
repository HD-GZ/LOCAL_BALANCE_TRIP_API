package live.lbtrip.domain.propensity.model;

import jakarta.persistence.Embedded;
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

    @Embedded
    private Preference preference;

    @Embedded
    private ValueConsumption valueConsumption;

    private Propensity(User user, Preference preference, ValueConsumption valueConsumption) {
        this.user = user;
        update(preference, valueConsumption);
    }

    public static Propensity create(User user, Preference preference, ValueConsumption valueConsumption) {
        return new Propensity(user, preference, valueConsumption);
    }

    public void update(Preference preference, ValueConsumption valueConsumption) {
        this.preference = preference;
        this.valueConsumption = valueConsumption;
    }
}
