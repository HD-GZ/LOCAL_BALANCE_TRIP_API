package live.lbtrip.domain.recommendation.model.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import live.lbtrip.global.model.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "odii_themes",
    uniqueConstraints = @UniqueConstraint(name = "uk_odii_themes", columnNames = {"tid", "tlid"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OdiiTheme extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String tid;

    @Column(nullable = false, length = 20)
    private String tlid;

    @Column(nullable = false, length = 200)
    private String title;

    private Double longitude;

    private Double latitude;

    @Column(name = "audio_url", length = 500)
    private String audioUrl;

    @Column(name = "audio_synced_at")
    private LocalDateTime audioSyncedAt;

    private OdiiTheme(String tid, String tlid, String title, Double longitude, Double latitude) {
        this.tid = tid;
        this.tlid = tlid;
        this.title = title;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public static OdiiTheme create(String tid, String tlid, String title, Double longitude, Double latitude) {
        return new OdiiTheme(tid, tlid, title, longitude, latitude);
    }

    public void update(String title, Double longitude, Double latitude) {
        this.title = title;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public void updateAudio(String audioUrl, LocalDateTime syncedAt) {
        this.audioUrl = audioUrl;
        this.audioSyncedAt = syncedAt;
    }
}
