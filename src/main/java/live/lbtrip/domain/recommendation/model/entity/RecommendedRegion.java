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
@Table(name = "recommended_regions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecommendedRegion extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "region_name", nullable = false, length = 50)
    private String regionName;

    @Column(name = "ldong_regn_cd", length = 2)
    private String ldongRegnCd;

    @Column(name = "ldong_signgu_cd", length = 3)
    private String ldongSignguCd;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(nullable = false, length = 300)
    private String reason;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @OrderBy("id asc")
    @OneToMany(mappedBy = "recommendedRegion", cascade = CascadeType.ALL)
    private List<GeneratedCourse> courses = new ArrayList<>();

    private RecommendedRegion(
        User user, String regionName, String ldongRegnCd, String ldongSignguCd,
        String imageUrl, String reason, int displayOrder
    ) {
        this.user = user;
        this.regionName = regionName;
        this.ldongRegnCd = ldongRegnCd;
        this.ldongSignguCd = ldongSignguCd;
        this.imageUrl = imageUrl;
        this.reason = reason;
        this.displayOrder = displayOrder;
    }

    public static RecommendedRegion create(
        User user, String regionName, String ldongRegnCd, String ldongSignguCd,
        String imageUrl, String reason, int displayOrder
    ) {
        return new RecommendedRegion(user, regionName, ldongRegnCd, ldongSignguCd, imageUrl, reason, displayOrder);
    }

    public void addCourse(GeneratedCourse course) {
        courses.add(course);
        course.assignRegion(this);
    }
}
