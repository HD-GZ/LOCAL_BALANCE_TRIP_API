package live.lbtrip.domain.propensity.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PropensityBucket localityBucket;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PropensityBucket frugalityBucket;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PropensityBucket experientialityBucket;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PropensityBucket vitalityBucket;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PropensityBucket socialityBucket;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PropensityBucket accommodationBucket;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PropensityBucket foodBucket;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PropensityBucket experienceBucket;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PropensityBucket transportationBucket;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PropensityBucket cafeExhibitionBucket;

    @Column(nullable = false, length = 100)
    private String type;

    @Column(nullable = false, length = 1000)
    private String description;

    private TravelProfile(
        PropensityBucket localityBucket,
        PropensityBucket frugalityBucket,
        PropensityBucket experientialityBucket,
        PropensityBucket vitalityBucket,
        PropensityBucket socialityBucket,
        PropensityBucket accommodationBucket,
        PropensityBucket foodBucket,
        PropensityBucket experienceBucket,
        PropensityBucket transportationBucket,
        PropensityBucket cafeExhibitionBucket,
        String type,
        String description
    ) {
        this.localityBucket = localityBucket;
        this.frugalityBucket = frugalityBucket;
        this.experientialityBucket = experientialityBucket;
        this.vitalityBucket = vitalityBucket;
        this.socialityBucket = socialityBucket;
        this.accommodationBucket = accommodationBucket;
        this.foodBucket = foodBucket;
        this.experienceBucket = experienceBucket;
        this.transportationBucket = transportationBucket;
        this.cafeExhibitionBucket = cafeExhibitionBucket;
        this.type = type;
        this.description = description;
    }

    public static TravelProfile create(
        PropensityBucket localityBucket,
        PropensityBucket frugalityBucket,
        PropensityBucket experientialityBucket,
        PropensityBucket vitalityBucket,
        PropensityBucket socialityBucket,
        PropensityBucket accommodationBucket,
        PropensityBucket foodBucket,
        PropensityBucket experienceBucket,
        PropensityBucket transportationBucket,
        PropensityBucket cafeExhibitionBucket,
        String type,
        String description
    ) {
        return new TravelProfile(
            localityBucket,
            frugalityBucket,
            experientialityBucket,
            vitalityBucket,
            socialityBucket,
            accommodationBucket,
            foodBucket,
            experienceBucket,
            transportationBucket,
            cafeExhibitionBucket,
            type,
            description
        );
    }
}
