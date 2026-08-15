package live.lbtrip.global.storage.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ImageDirectory {

    RECEIPT("receipts");

    private final String path;
}
