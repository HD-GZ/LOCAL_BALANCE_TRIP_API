package live.lbtrip.global.storage.vo;

import org.springframework.http.MediaType;

public record ValidatedImage(
    byte[] bytes,
    String extension,
    MediaType mediaType
) {

    public static ValidatedImage of(byte[] bytes, String extension, MediaType mediaType) {
        return new ValidatedImage(bytes, extension, mediaType);
    }

    public long size() {
        return bytes.length;
    }
}
