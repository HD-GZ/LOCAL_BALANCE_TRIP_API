package live.lbtrip.domain.savedcourse.model.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import live.lbtrip.global.model.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "saved_course_places")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SavedCoursePlace extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "saved_course_id", nullable = false)
    private SavedCourse savedCourse;

    @Column(name = "visit_order", nullable = false)
    private int visitOrder;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String overview;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    private Double latitude;

    private Double longitude;

    @Column(name = "walk_minutes")
    private Integer walkMinutes;

    @Column(name = "has_audio", nullable = false)
    private boolean hasAudio;

    @Column(name = "audio_url", length = 500)
    private String audioUrl;

    @Column(name = "visited_at", columnDefinition = "TIMESTAMP")
    private LocalDateTime visitedAt;

    private SavedCoursePlace(
        int visitOrder, String name, String overview, String imageUrl,
        Double latitude, Double longitude, Integer walkMinutes, boolean hasAudio, String audioUrl
    ) {
        this.visitOrder = visitOrder;
        this.name = name;
        this.overview = overview;
        this.imageUrl = imageUrl;
        this.latitude = latitude;
        this.longitude = longitude;
        this.walkMinutes = walkMinutes;
        this.hasAudio = hasAudio;
        this.audioUrl = audioUrl;
    }

    public static SavedCoursePlace create(
        int visitOrder, String name, String overview, String imageUrl,
        Double latitude, Double longitude, Integer walkMinutes, boolean hasAudio, String audioUrl
    ) {
        return new SavedCoursePlace(visitOrder, name, overview, imageUrl,
            latitude, longitude, walkMinutes, hasAudio, audioUrl);
    }

    void assignSavedCourse(SavedCourse savedCourse) {
        this.savedCourse = savedCourse;
    }

    public boolean isVisited() {
        return visitedAt != null;
    }

    void checkIn() {
        if (visitedAt == null) {
            this.visitedAt = LocalDateTime.now();
        }
    }
}
