package live.lbtrip.domain.savedcourse.model.entity;

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
import jakarta.persistence.Table;
import live.lbtrip.domain.savedcourse.model.ImagePurpose;
import live.lbtrip.domain.savedcourse.model.StoredImageStatus;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import live.lbtrip.global.model.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "images")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StoredImage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "saved_course_id", nullable = false)
    private SavedCourse savedCourse;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ImagePurpose purpose;

    @Column(name = "storage_key", nullable = false, unique = true, length = 300)
    private String storageKey;

    @Column(name = "content_type", length = 50)
    private String contentType;

    @Column(name = "file_size", nullable = false)
    private long fileSize;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StoredImageStatus status;

    private StoredImage(
        SavedCourse savedCourse,
        ImagePurpose purpose,
        String storageKey,
        String contentType,
        long fileSize,
        StoredImageStatus status
    ) {
        this.savedCourse = savedCourse;
        this.purpose = purpose;
        this.storageKey = storageKey;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.status = status;
    }

    public static StoredImage createReceipt(
        SavedCourse savedCourse,
        String storageKey,
        String contentType,
        long fileSize
    ) {
        return new StoredImage(
            savedCourse,
            ImagePurpose.RECEIPT,
            storageKey,
            contentType,
            fileSize,
            StoredImageStatus.PENDING
        );
    }

    public void attach() {
        if (status == StoredImageStatus.ATTACHED) {
            throw BusinessException.of(ErrorCode.RECEIPT_IMAGE_ALREADY_USED);
        }
        this.status = StoredImageStatus.ATTACHED;
    }
}
