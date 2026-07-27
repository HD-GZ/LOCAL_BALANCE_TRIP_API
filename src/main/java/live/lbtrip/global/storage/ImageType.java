package live.lbtrip.global.storage;

import java.util.Set;

import org.springframework.http.MediaType;

public enum ImageType {

    JPEG(MediaType.IMAGE_JPEG, Set.of("jpg", "jpeg")),
    PNG(MediaType.IMAGE_PNG, Set.of("png")),
    WEBP(MediaType.parseMediaType("image/webp"), Set.of("webp"));

    private final MediaType mediaType;
    private final Set<String> extensions;

    ImageType(MediaType mediaType, Set<String> extensions) {
        this.mediaType = mediaType;
        this.extensions = extensions;
    }

    public MediaType mediaType() {
        return mediaType;
    }

    public boolean supportsExtension(String extension) {
        return extensions.contains(extension);
    }
}
