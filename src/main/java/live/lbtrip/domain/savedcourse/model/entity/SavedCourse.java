package live.lbtrip.domain.savedcourse.model.entity;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import live.lbtrip.domain.savedcourse.model.enums.SavedCourseStatus;
import live.lbtrip.domain.user.model.User;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import live.lbtrip.global.model.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "saved_courses")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SavedCourse extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "source_course_id", nullable = false)
    private Long sourceCourseId;

    @Column(name = "course_name", nullable = false, length = 100)
    private String courseName;

    @Column(name = "region_name", nullable = false, length = 50)
    private String regionName;

    @Column(nullable = false, length = 300)
    private String reason;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "ldong_regn_cd", length = 2)
    private String ldongRegnCd;

    @Column(name = "ldong_signgu_cd", length = 3)
    private String ldongSignguCd;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SavedCourseStatus status;

    @Column(name = "tour_started_at", columnDefinition = "TIMESTAMP")
    private LocalDateTime tourStartedAt;

    @Column(name = "tour_ended_at", columnDefinition = "TIMESTAMP")
    private LocalDateTime tourEndedAt;

    @OneToMany(mappedBy = "savedCourse", cascade = CascadeType.ALL)
    @OrderBy("visitOrder asc")
    private List<SavedCoursePlace> places = new ArrayList<>();

    @OneToMany(mappedBy = "savedCourse")
    @OrderBy("id desc")
    private List<TourReceipt> receipts = new ArrayList<>();

    private SavedCourse(
        User user, Long sourceCourseId, String courseName, String regionName, String reason,
        String imageUrl, String ldongRegnCd, String ldongSignguCd
    ) {
        this.user = user;
        this.sourceCourseId = sourceCourseId;
        this.courseName = courseName;
        this.regionName = regionName;
        this.reason = reason;
        this.imageUrl = imageUrl;
        this.ldongRegnCd = ldongRegnCd;
        this.ldongSignguCd = ldongSignguCd;
        this.status = SavedCourseStatus.BEFORE_TRIP;
    }

    public static SavedCourse create(
        User user, Long sourceCourseId, String courseName, String regionName, String reason,
        String imageUrl, String ldongRegnCd, String ldongSignguCd
    ) {
        return new SavedCourse(user, sourceCourseId, courseName, regionName, reason,
            imageUrl, ldongRegnCd, ldongSignguCd);
    }

    public void changeStatus(SavedCourseStatus status) {
        this.status = status;
    }

    public void addPlace(SavedCoursePlace place) {
        places.add(place);
        place.assignSavedCourse(this);
    }

    void addReceipt(TourReceipt receipt) {
        receipts.add(receipt);
    }

    public int calculateTotalReceiptAmount() {
        return receipts.stream()
            .mapToInt(TourReceipt::getAmount)
            .sum();
    }

    public TourReceipt findReceiptById(Long receiptId) {
        return receipts.stream()
            .filter(receipt -> Objects.equals(receipt.getId(), receiptId))
            .findFirst()
            .orElseThrow(() -> BusinessException.of(ErrorCode.TOUR_RECEIPT_NOT_FOUND));
    }

    public void startTour() {
        if (status == SavedCourseStatus.COMPLETED) {
            throw BusinessException.of(ErrorCode.TOUR_ALREADY_COMPLETED);
        }
        this.status = SavedCourseStatus.TRAVELING;
        if (tourStartedAt == null) {
            this.tourStartedAt = LocalDateTime.now();
        }
        this.tourEndedAt = null;
    }

    public void checkInPlace(Long placeId) {
        validateTraveling();
        places.stream()
            .filter(p -> p.getId().equals(placeId))
            .findFirst()
            .orElseThrow(() -> BusinessException.of(ErrorCode.SAVED_COURSE_PLACE_NOT_FOUND))
            .checkIn();
    }

    public boolean endTour() {
        validateTraveling();
        this.tourEndedAt = LocalDateTime.now();
        boolean completed = places.stream().allMatch(SavedCoursePlace::isVisited);
        if (completed) {
            this.status = SavedCourseStatus.COMPLETED;
        }
        return completed;
    }

    public int countVisitedPlaces() {
        return (int) places.stream().filter(SavedCoursePlace::isVisited).count();
    }

    public long tourDurationMinutes() {
        if (tourStartedAt == null || tourEndedAt == null) {
            return 0;
        }
        return Duration.between(tourStartedAt, tourEndedAt).toMinutes();
    }

    private void validateTraveling() {
        if (status != SavedCourseStatus.TRAVELING) {
            throw BusinessException.of(ErrorCode.TOUR_NOT_IN_PROGRESS);
        }
    }
}
