package live.lbtrip.domain.tourism.model.enums;

import java.util.Arrays;
import java.util.Optional;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum VisitorType {

    LOCAL("1"),
    OUTSIDER("2"),
    FOREIGNER("3");

    private final String code;

    public static Optional<VisitorType> fromCode(String code) {
        return Arrays.stream(values())
            .filter(type -> type.code.equals(code))
            .findFirst();
    }
}
