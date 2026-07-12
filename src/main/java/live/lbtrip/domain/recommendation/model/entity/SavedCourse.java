package live.lbtrip.domain.recommendation.model.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import live.lbtrip.domain.user.model.User;
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

    @OneToMany(mappedBy = "savedCourse", cascade = CascadeType.ALL)
    private List<SavedCoursePlace> places = new ArrayList<>();

    private SavedCourse(User user, Long sourceCourseId, String courseName, String regionName, String reason) {
        this.user = user;
        this.sourceCourseId = sourceCourseId;
        this.courseName = courseName;
        this.regionName = regionName;
        this.reason = reason;
    }

    public static SavedCourse create(
        User user, Long sourceCourseId, String courseName, String regionName, String reason
    ) {
        return new SavedCourse(user, sourceCourseId, courseName, regionName, reason);
    }

    public void addPlace(SavedCoursePlace place) {
        places.add(place);
        place.assignSavedCourse(this);
    }
}
