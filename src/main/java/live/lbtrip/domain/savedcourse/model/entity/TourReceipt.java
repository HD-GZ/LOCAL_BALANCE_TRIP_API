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
import jakarta.persistence.Table;
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

    @Column(name = "image_key", nullable = false, length = 300)
    private String imageKey;

    private TourReceipt(SavedCourse savedCourse, String merchantName, int amount, LocalDate paidDate, String imageKey) {
        this.savedCourse = savedCourse;
        this.merchantName = merchantName;
        this.amount = amount;
        this.paidDate = paidDate;
        this.imageKey = imageKey;
    }

    public static TourReceipt create(
        SavedCourse savedCourse, String merchantName, int amount, LocalDate paidDate, String imageKey
    ) {
        return new TourReceipt(savedCourse, merchantName, amount, paidDate, imageKey);
    }
}
