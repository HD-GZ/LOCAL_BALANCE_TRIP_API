package live.lbtrip.domain.savedcourse.share.model.entity;

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
import live.lbtrip.domain.savedcourse.model.entity.SavedCourse;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import live.lbtrip.global.model.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "course_share_tokens")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CourseShareToken extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "saved_course_id", nullable = false)
    private SavedCourse savedCourse;

    @Column(nullable = false, unique = true, length = 36)
    private String token;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    private CourseShareToken(SavedCourse savedCourse, String token, LocalDateTime expiresAt) {
        this.savedCourse = savedCourse;
        this.token = token;
        this.expiresAt = expiresAt;
    }

    public static CourseShareToken create(SavedCourse savedCourse, String token, LocalDateTime expiresAt) {
        return new CourseShareToken(savedCourse, token, expiresAt);
    }

    public void validateUsable(LocalDateTime now) {
        if (expiresAt.isBefore(now)) {
            throw BusinessException.of(ErrorCode.SHARE_TOKEN_EXPIRED);
        }
    }
}
