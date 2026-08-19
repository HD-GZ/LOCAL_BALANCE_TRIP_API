package live.lbtrip.domain.tourism.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import live.lbtrip.domain.region.model.RegionCandidate;
import live.lbtrip.global.model.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "tour_places",
    uniqueConstraints = @UniqueConstraint(name = "uk_tour_places_content_id", columnNames = "content_id"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TourPlace extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "content_id", nullable = false, length = 20)
    private String contentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_candidate_id", nullable = false)
    private RegionCandidate regionCandidate;

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

    @Column(name = "eng_content_id", length = 20)
    private String engContentId;

    @Column(name = "title_en", length = 200)
    private String titleEn;

    @Column(name = "overview_en", columnDefinition = "TEXT")
    private String overviewEn;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "odii_theme_id")
    private OdiiTheme odiiTheme;

    private TourPlace(
        String contentId, RegionCandidate regionCandidate, int contentTypeId,
        String title, String imageUrl, Double longitude, Double latitude, int sortOrder
    ) {
        this.contentId = contentId;
        this.regionCandidate = regionCandidate;
        this.contentTypeId = contentTypeId;
        this.title = title;
        this.imageUrl = imageUrl;
        this.longitude = longitude;
        this.latitude = latitude;
        this.sortOrder = sortOrder;
    }

    public static TourPlace create(
        String contentId, RegionCandidate regionCandidate, int contentTypeId,
        String title, String imageUrl, Double longitude, Double latitude, int sortOrder
    ) {
        return new TourPlace(
            contentId, regionCandidate, contentTypeId,
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

    public void updateEnglish(String engContentId, String titleEn) {
        this.engContentId = engContentId;
        this.titleEn = titleEn;
    }

    public void updateEnglishOverview(String overviewEn) {
        this.overviewEn = overviewEn;
    }

    public void assignOdiiTheme(OdiiTheme odiiTheme) {
        this.odiiTheme = odiiTheme;
    }
}
