package live.lbtrip.domain.savedcourse.model.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import live.lbtrip.domain.image.model.entity.Image;
import live.lbtrip.global.model.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "tour_receipts")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TourReceipt extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "saved_course_id", nullable = false)
    private SavedCourse savedCourse;

    @Column(name = "merchant_name", nullable = false, length = 100)
    private String merchantName;

    @Column(nullable = false)
    private int amount;

    @Column(name = "paid_date", nullable = false)
    private LocalDate paidDate;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_id", nullable = false, unique = true)
    private Image image;

    private TourReceipt(SavedCourse savedCourse, String merchantName, int amount, LocalDate paidDate, Image image) {
        this.savedCourse = savedCourse;
        this.merchantName = merchantName;
        this.amount = amount;
        this.paidDate = paidDate;
        this.image = image;
        savedCourse.addReceipt(this);
    }

    public static TourReceipt create(
        SavedCourse savedCourse, String merchantName, int amount, LocalDate paidDate, Image image
    ) {
        return new TourReceipt(savedCourse, merchantName, amount, paidDate, image);
    }

    public void update(String merchantName, int amount, LocalDate paidDate) {
        this.merchantName = merchantName;
        this.amount = amount;
        this.paidDate = paidDate;
    }
}
