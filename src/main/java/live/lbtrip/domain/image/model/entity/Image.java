package live.lbtrip.domain.image.model.entity;

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
import live.lbtrip.domain.image.model.ImageStatus;
import live.lbtrip.domain.user.model.User;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import live.lbtrip.global.model.BaseEntity;
import live.lbtrip.global.storage.ImageDirectory;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "images")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Image extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploader_id", nullable = false)
    private User uploader;

    @Enumerated(EnumType.STRING)
    @Column(name = "storage_directory", nullable = false, length = 30)
    private ImageDirectory directory;

    @Column(name = "storage_key", nullable = false, unique = true, length = 300)
    private String storageKey;

    @Column(name = "content_type", nullable = false, length = 50)
    private String contentType;

    @Column(name = "file_size", nullable = false)
    private long fileSize;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ImageStatus status;

    private Image(
        User uploader,
        ImageDirectory directory,
        String storageKey,
        String contentType,
        long fileSize,
        ImageStatus status
    ) {
        this.uploader = uploader;
        this.directory = directory;
        this.storageKey = storageKey;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.status = status;
    }

    public static Image create(
        User uploader,
        ImageDirectory directory,
        String storageKey,
        String contentType,
        long fileSize
    ) {
        return new Image(
            uploader,
            directory,
            storageKey,
            contentType,
            fileSize,
            ImageStatus.TEMPORARY
        );
    }

    public void attach() {
        if (status == ImageStatus.ATTACHED) {
            throw BusinessException.of(ErrorCode.IMAGE_ALREADY_ATTACHED);
        }
        this.status = ImageStatus.ATTACHED;
    }
}
