package live.lbtrip.domain.propensity.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import live.lbtrip.domain.propensity.dto.request.PropensityRequest;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Preference {

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

    private Preference(
        int locality,
        int frugality,
        int experientiality,
        int vitality,
        int sociality
    ) {
        this.locality = locality;
        this.frugality = frugality;
        this.experientiality = experientiality;
        this.vitality = vitality;
        this.sociality = sociality;
    }

    public static Preference of(
        int locality,
        int frugality,
        int experientiality,
        int vitality,
        int sociality
    ) {
        return new Preference(locality, frugality, experientiality, vitality, sociality);
    }

    public static Preference from(PropensityRequest.InnerPreferenceRequest request) {
        return of(
            request.locality(),
            request.frugality(),
            request.experientiality(),
            request.vitality(),
            request.sociality()
        );
    }
}
