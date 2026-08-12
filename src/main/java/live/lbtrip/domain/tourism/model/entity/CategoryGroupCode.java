package live.lbtrip.domain.tourism.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import live.lbtrip.domain.tourism.model.enums.CategoryGroup;
import live.lbtrip.domain.tourism.model.enums.CodeLevel;
import live.lbtrip.global.model.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "category_group_codes",
    uniqueConstraints = @UniqueConstraint(name = "uk_category_group_codes",
        columnNames = {"category_group", "code_level", "code"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CategoryGroupCode extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "category_group", nullable = false, length = 30)
    private CategoryGroup categoryGroup;

    @Enumerated(EnumType.STRING)
    @Column(name = "code_level", nullable = false, length = 10)
    private CodeLevel codeLevel;

    @Column(name = "code", nullable = false, length = 10)
    private String code;

    private CategoryGroupCode(CategoryGroup categoryGroup, CodeLevel codeLevel, String code) {
        this.categoryGroup = categoryGroup;
        this.codeLevel = codeLevel;
        this.code = code;
    }

    public static CategoryGroupCode create(CategoryGroup categoryGroup, CodeLevel codeLevel, String code) {
        return new CategoryGroupCode(categoryGroup, codeLevel, code);
    }
}
