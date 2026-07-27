package live.lbtrip.global.storage;

import org.springframework.http.MediaType;

public record ValidatedImage(
    byte[] bytes,
    String extension,
    MediaType mediaType
) {

    public long size() {
        return bytes.length;
    }
}
