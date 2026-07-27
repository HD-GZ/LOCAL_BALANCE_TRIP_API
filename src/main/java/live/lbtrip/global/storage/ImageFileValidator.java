package live.lbtrip.global.storage;

import java.io.IOException;
import java.util.Locale;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import live.lbtrip.global.config.StorageProperties;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import live.lbtrip.global.storage.enums.ImageType;
import live.lbtrip.global.storage.vo.ValidatedImage;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ImageFileValidator {

    private final StorageProperties properties;

    public ValidatedImage validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw BusinessException.of(ErrorCode.INVALID_INPUT_VALUE);
        }
        if (file.getSize() > properties.maxImageSize().toBytes()) {
            throw BusinessException.of(ErrorCode.IMAGE_SIZE_EXCEEDED);
        }

        String extension = extractExtension(file.getOriginalFilename());
        byte[] bytes = readBytes(file);
        ImageType imageType = detectImageType(bytes);
        if (!imageType.supportsExtension(extension)) {
            throw BusinessException.of(ErrorCode.INVALID_IMAGE_TYPE);
        }
        return ValidatedImage.of(bytes, extension, imageType.getMediaType());
    }

    private String extractExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            throw BusinessException.of(ErrorCode.INVALID_IMAGE_TYPE);
        }
        String extension = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
        if (extension.isBlank()) {
            throw BusinessException.of(ErrorCode.INVALID_IMAGE_TYPE);
        }
        return extension;
    }

    private byte[] readBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException e) {
            throw BusinessException.of(ErrorCode.IMAGE_UPLOAD_FAILED);
        }
    }

    private ImageType detectImageType(byte[] bytes) {
        if (isJpeg(bytes)) {
            return ImageType.JPEG;
        }
        if (isPng(bytes)) {
            return ImageType.PNG;
        }
        if (isWebp(bytes)) {
            return ImageType.WEBP;
        }
        throw BusinessException.of(ErrorCode.INVALID_IMAGE_TYPE);
    }

    private boolean isJpeg(byte[] bytes) {
        return bytes.length >= 3
            && unsigned(bytes[0]) == 0xFF
            && unsigned(bytes[1]) == 0xD8
            && unsigned(bytes[2]) == 0xFF;
    }

    private boolean isPng(byte[] bytes) {
        int[] signature = {0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
        if (bytes.length < signature.length) {
            return false;
        }
        for (int index = 0; index < signature.length; index++) {
            if (unsigned(bytes[index]) != signature[index]) {
                return false;
            }
        }
        return true;
    }

    private boolean isWebp(byte[] bytes) {
        return bytes.length >= 12
            && matchesAscii(bytes, 0, "RIFF")
            && matchesAscii(bytes, 8, "WEBP");
    }

    private boolean matchesAscii(byte[] bytes, int offset, String value) {
        for (int index = 0; index < value.length(); index++) {
            if (bytes[offset + index] != value.charAt(index)) {
                return false;
            }
        }
        return true;
    }

    private int unsigned(byte value) {
        return Byte.toUnsignedInt(value);
    }
}
