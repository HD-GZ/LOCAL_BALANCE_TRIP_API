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
public class ValueConsumption {

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

    private ValueConsumption(
        int accommodation,
        int food,
        int experience,
        int transportation,
        int cafeExhibition
    ) {
        this.accommodation = accommodation;
        this.food = food;
        this.experience = experience;
        this.transportation = transportation;
        this.cafeExhibition = cafeExhibition;
    }

    public static ValueConsumption of(
        int accommodation,
        int food,
        int experience,
        int transportation,
        int cafeExhibition
    ) {
        return new ValueConsumption(accommodation, food, experience, transportation, cafeExhibition);
    }

    public static ValueConsumption from(PropensityRequest.InnerValueConsumptionRequest request) {
        return of(
            request.accommodation(),
            request.food(),
            request.experience(),
            request.transportation(),
            request.cafeExhibition()
        );
    }
}
