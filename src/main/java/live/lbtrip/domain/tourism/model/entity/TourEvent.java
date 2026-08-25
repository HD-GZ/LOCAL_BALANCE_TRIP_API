package live.lbtrip.domain.tourism.model.entity;

import java.time.LocalDate;
import java.util.Locale;

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
@Table(name = "tour_events",
    uniqueConstraints = @UniqueConstraint(name = "uk_tour_events_locale_content_id", columnNames = {"locale", "content_id"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TourEvent extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 5)
    private Locale locale;

    @Column(name = "content_id", nullable = false, length = 20)
    private String contentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_candidate_id", nullable = false)
    private RegionCandidate regionCandidate;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    private Double latitude;

    private Double longitude;

    @Column(length = 300)
    private String address;

    @Column(name = "event_start", nullable = false)
    private LocalDate eventStart;

    @Column(name = "event_end", nullable = false)
    private LocalDate eventEnd;

    private TourEvent(
        Locale locale, String contentId, RegionCandidate regionCandidate, String title, String imageUrl,
        Double latitude, Double longitude, String address, LocalDate eventStart, LocalDate eventEnd
    ) {
        this.locale = locale;
        this.contentId = contentId;
        this.regionCandidate = regionCandidate;
        this.title = title;
        this.imageUrl = imageUrl;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
        this.eventStart = eventStart;
        this.eventEnd = eventEnd;
    }

    public static TourEvent create(
        Locale locale, String contentId, RegionCandidate regionCandidate, String title, String imageUrl,
        Double latitude, Double longitude, String address, LocalDate eventStart, LocalDate eventEnd
    ) {
        return new TourEvent(locale, contentId, regionCandidate, title, imageUrl,
            latitude, longitude, address, eventStart, eventEnd);
    }

    public void update(
        String title, String imageUrl, Double latitude, Double longitude, String address,
        LocalDate eventStart, LocalDate eventEnd
    ) {
        this.title = title;
        this.imageUrl = imageUrl;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
        this.eventStart = eventStart;
        this.eventEnd = eventEnd;
    }
}
