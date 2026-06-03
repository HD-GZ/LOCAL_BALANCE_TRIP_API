package live.lbtrip.global.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@MappedSuperclass
@EntityListeners(value = AuditingEntityListener.class)
public abstract class BaseEntity {

	@NotNull
	@CreatedDate
	@Column(name = "created_at", columnDefinition = "TIMESTAMP", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@NotNull
	@LastModifiedDate
	@Column(name = "updated_at", columnDefinition = "TIMESTAMP", nullable = false, updatable = true)
	private LocalDateTime updatedAt;
}
