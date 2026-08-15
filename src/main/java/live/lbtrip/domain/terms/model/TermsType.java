package live.lbtrip.domain.terms.model;

import java.util.Arrays;

import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import live.lbtrip.global.util.StringNormalizer;

public enum TermsType {

    SERVICE,
    PRIVACY,
    MARKETING;

    public static TermsType from(String value) {
        String normalized = StringNormalizer.trim(value);
        return Arrays.stream(values())
            .filter(type -> type.name().equalsIgnoreCase(normalized))
            .findFirst()
            .orElseThrow(() -> BusinessException.of(ErrorCode.TERMS_NOT_FOUND));
    }
}
