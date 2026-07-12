package live.lbtrip.domain.recommendation.model.entity;

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

/**
 * 코스를 구성하는 장소의 화면용 스냅샷(추천 생성 시점 고정).
 * TourAPI content_id는 저장하지 않는다 — 이 스냅샷이 유일한 원천이다. (스펙 §2)
 */
@Getter
@Entity
@Table(name = "course_places")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CoursePlace extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private GeneratedCourse course;

    /** 방문 순서(1부터). */
    @Column(name = "visit_order", nullable = false)
    private int visitOrder;

    @Column(nullable = false, length = 100)
    private String name;

    /** 장소 소개(TourAPI overview 스냅샷). */
    @Column(columnDefinition = "TEXT")
    private String overview;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    private Double latitude;

    private Double longitude;

    /** 이전 장소로부터 도보 이동 시간(분). 첫 장소는 null. */
    @Column(name = "walk_minutes")
    private Integer walkMinutes;

    @Column(name = "has_audio", nullable = false)
    private boolean hasAudio;

    @Column(name = "audio_url", length = 500)
    private String audioUrl;

    private CoursePlace(
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

    public static CoursePlace create(
        int visitOrder, String name, String overview, String imageUrl,
        Double latitude, Double longitude, Integer walkMinutes, boolean hasAudio, String audioUrl
    ) {
        return new CoursePlace(visitOrder, name, overview, imageUrl,
            latitude, longitude, walkMinutes, hasAudio, audioUrl);
    }

    /** {@link GeneratedCourse#addPlace}에서만 호출하는 연관관계 세터. */
    void assignCourse(GeneratedCourse course) {
        this.course = course;
    }
}
