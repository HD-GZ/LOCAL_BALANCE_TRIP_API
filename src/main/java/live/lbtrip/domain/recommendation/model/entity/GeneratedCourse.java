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
@Table(name = "generated_courses")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GeneratedCourse extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recommended_region_id", nullable = false)
    private RecommendedRegion recommendedRegion;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 300)
    private String reason;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL)
    @OrderBy("visitOrder asc")
    private List<CoursePlace> places = new ArrayList<>();

    private GeneratedCourse(User user, String name, String reason, String imageUrl, int displayOrder) {
        this.user = user;
        this.name = name;
        this.reason = reason;
        this.imageUrl = imageUrl;
        this.displayOrder = displayOrder;
    }

    public static GeneratedCourse create(
        User user, String name, String reason, String imageUrl, int displayOrder
    ) {
        return new GeneratedCourse(user, name, reason, imageUrl, displayOrder);
    }

    void assignRegion(RecommendedRegion recommendedRegion) {
        this.recommendedRegion = recommendedRegion;
    }

    public void addPlace(CoursePlace place) {
        places.add(place);
        place.assignCourse(this);
    }
}
