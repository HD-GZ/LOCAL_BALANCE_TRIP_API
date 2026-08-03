package live.lbtrip.domain.terms.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import live.lbtrip.global.model.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "terms")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Terms extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TermsType type;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, length = 20)
    private String version;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private LocalDate effectiveDate;

    private Terms(TermsType type, String title, String version, String content, LocalDate effectiveDate) {
        this.type = type;
        this.title = title;
        this.version = version;
        this.content = content;
        this.effectiveDate = effectiveDate;
    }

    public static Terms create(
        TermsType type,
        String title,
        String version,
        String content,
        LocalDate effectiveDate
    ) {
        return new Terms(type, title, version, content, effectiveDate);
    }
}
