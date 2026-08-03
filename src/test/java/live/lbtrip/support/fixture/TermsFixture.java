package live.lbtrip.support.fixture;

import java.time.LocalDate;

import live.lbtrip.domain.terms.dto.response.TermsResponse;
import live.lbtrip.domain.terms.model.Terms;
import live.lbtrip.domain.terms.model.TermsType;

public final class TermsFixture {

    public static final TermsType TYPE = TermsType.SERVICE;
    public static final String TYPE_PATH = "service";
    public static final String UNSUPPORTED_TYPE_PATH = "refund";
    public static final String TITLE = "서비스 이용약관";
    public static final String VERSION = "1.0";
    public static final String CONTENT = """
        ## 제1조 (목적)
        본 약관은 로컬밸런스 트립이 제공하는 서비스의 이용 조건과 절차를 정하는 것을 목적으로 해요.""";
    public static final LocalDate EFFECTIVE_DATE = LocalDate.of(2026, 7, 1);

    private TermsFixture() {
    }

    public static Terms terms() {
        return Terms.create(TYPE, TITLE, VERSION, CONTENT, EFFECTIVE_DATE);
    }

    public static TermsResponse termsResponse() {
        return TermsResponse.from(terms());
    }
}
