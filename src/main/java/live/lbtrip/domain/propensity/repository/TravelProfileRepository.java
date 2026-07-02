package live.lbtrip.domain.propensity.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import live.lbtrip.domain.propensity.model.PropensityBucket;
import live.lbtrip.domain.propensity.model.TravelProfile;

public interface TravelProfileRepository extends JpaRepository<TravelProfile, Long> {

    @Query("""
        SELECT tp
        FROM TravelProfile tp
        WHERE tp.localityBucket = :localityBucket
          AND tp.frugalityBucket = :frugalityBucket
          AND tp.experientialityBucket = :experientialityBucket
          AND tp.vitalityBucket = :vitalityBucket
          AND tp.socialityBucket = :socialityBucket
          AND tp.accommodationBucket = :accommodationBucket
          AND tp.foodBucket = :foodBucket
          AND tp.experienceBucket = :experienceBucket
          AND tp.transportationBucket = :transportationBucket
          AND tp.cafeExhibitionBucket = :cafeExhibitionBucket
        """)
    Optional<TravelProfile> findByBuckets(
        @Param("localityBucket") PropensityBucket localityBucket,
        @Param("frugalityBucket") PropensityBucket frugalityBucket,
        @Param("experientialityBucket") PropensityBucket experientialityBucket,
        @Param("vitalityBucket") PropensityBucket vitalityBucket,
        @Param("socialityBucket") PropensityBucket socialityBucket,
        @Param("accommodationBucket") PropensityBucket accommodationBucket,
        @Param("foodBucket") PropensityBucket foodBucket,
        @Param("experienceBucket") PropensityBucket experienceBucket,
        @Param("transportationBucket") PropensityBucket transportationBucket,
        @Param("cafeExhibitionBucket") PropensityBucket cafeExhibitionBucket
    );
}
