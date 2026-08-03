package live.lbtrip.domain.terms.dto.response;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import live.lbtrip.domain.terms.model.Terms;
import live.lbtrip.domain.terms.model.TermsType;

public record TermsResponse(
    @Schema(description = "약관 종류", example = "SERVICE")
    TermsType type,

    @Schema(description = "약관 제목", example = "서비스 이용약관")
    String title,

    @Schema(description = "약관 버전", example = "1.0")
    String version,

    @Schema(description = "시행일", example = "2026-07-01")
    LocalDate effectiveDate,

    @Schema(
        description = "약관 전문. 마크다운 형식이며 조항 제목은 ##, 나열 항목은 -로 표기됩니다.",
        example = "## 제1조 (목적)\n본 약관은 로컬밸런스 트립이 제공하는 서비스의 이용 조건과 절차를 정하는 것을 목적으로 해요."
    )
    String content
) {

    public static TermsResponse from(Terms terms) {
        return new TermsResponse(
            terms.getType(),
            terms.getTitle(),
            terms.getVersion(),
            terms.getEffectiveDate(),
            terms.getContent()
        );
    }
}
