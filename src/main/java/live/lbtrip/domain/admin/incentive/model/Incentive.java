package live.lbtrip.domain.admin.incentive.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import live.lbtrip.global.model.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "incentives")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Incentive extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "행사 제목은 필수입니다.")
    @Size(max = 200, message = "행사 제목은 200자 이하여야 합니다.")
    @Column(nullable = false, length = 200)
    private String title;

    @NotBlank(message = "행사 페이지 URL은 필수입니다.")
    @Size(max = 500, message = "행사 페이지 URL은 500자 이하여야 합니다.")
    @Column(nullable = false, length = 500)
    private String url;

    private Incentive(String title, String url) {
        this.title = title;
        this.url = url;
    }

    public static Incentive create(String title, String url) {
        return new Incentive(title, url);
    }

    public void update(String title, String url) {
        this.title = title;
        this.url = url;
    }
}
