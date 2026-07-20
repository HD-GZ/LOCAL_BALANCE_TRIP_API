package live.lbtrip.domain.recommendation.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import live.lbtrip.global.model.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "tour_places",
    uniqueConstraints = @UniqueConstraint(name = "uk_tour_places_content_id", columnNames = "content_id"),
    indexes = @Index(name = "idx_tour_places_region",
        columnList = "ldong_regn_cd, ldong_signgu_cd, content_type_id"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TourPlace extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "content_id", nullable = false, length = 20)
    private String contentId;

    @Column(name = "ldong_regn_cd", nullable = false, length = 2)
    private String ldongRegnCd;

    @Column(name = "ldong_signgu_cd", nullable = false, length = 3)
    private String ldongSignguCd;

    @Column(name = "content_type_id", nullable = false)
    private int contentTypeId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    private Double longitude;

    private Double latitude;

    @Column(columnDefinition = "TEXT")
    private String overview;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    private TourPlace(
        String contentId, String ldongRegnCd, String ldongSignguCd, int contentTypeId,
        String title, String imageUrl, Double longitude, Double latitude, int sortOrder
    ) {
        this.contentId = contentId;
        this.ldongRegnCd = ldongRegnCd;
        this.ldongSignguCd = ldongSignguCd;
        this.contentTypeId = contentTypeId;
        this.title = title;
        this.imageUrl = imageUrl;
        this.longitude = longitude;
        this.latitude = latitude;
        this.sortOrder = sortOrder;
    }

    public static TourPlace create(
        String contentId, String ldongRegnCd, String ldongSignguCd, int contentTypeId,
        String title, String imageUrl, Double longitude, Double latitude, int sortOrder
    ) {
        return new TourPlace(
            contentId, ldongRegnCd, ldongSignguCd, contentTypeId,
            title, imageUrl, longitude, latitude, sortOrder);
    }

    public void update(String title, String imageUrl, Double longitude, Double latitude, int sortOrder) {
        this.title = title;
        this.imageUrl = imageUrl;
        this.longitude = longitude;
        this.latitude = latitude;
        this.sortOrder = sortOrder;
    }

    public void updateOverview(String overview) {
        this.overview = overview;
    }
}
