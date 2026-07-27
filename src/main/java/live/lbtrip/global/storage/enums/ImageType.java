package live.lbtrip.global.storage.enums;

import java.util.Set;

import org.springframework.http.MediaType;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ImageType {

    JPEG(MediaType.IMAGE_JPEG, Set.of("jpg", "jpeg")),
    PNG(MediaType.IMAGE_PNG, Set.of("png")),
    WEBP(MediaType.parseMediaType("image/webp"), Set.of("webp"));

    private final MediaType mediaType;
    private final Set<String> extensions;

    public boolean supportsExtension(String extension) {
        return extensions.contains(extension);
    }
}
