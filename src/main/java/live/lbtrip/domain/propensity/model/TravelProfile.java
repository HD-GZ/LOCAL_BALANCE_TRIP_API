package live.lbtrip.domain.propensity.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "travel_profiles")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TravelProfile extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 5, unique = true)
    private String code;

    @Column(nullable = false, length = 100)
    private String nickname;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(nullable = false, length = 255)
    private String imageKey;

    private TravelProfile(String code, String nickname, String description, String imageKey) {
        this.code = code;
        this.nickname = nickname;
        this.description = description;
        this.imageKey = imageKey;
    }

    public static TravelProfile create(String code, String nickname, String description, String imageKey) {
        return new TravelProfile(code, nickname, description, imageKey);
    }
}
