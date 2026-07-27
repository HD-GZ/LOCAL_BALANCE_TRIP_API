package live.lbtrip.domain.savedcourse.service;

import static live.lbtrip.domain.savedcourse.model.ImagePurpose.RECEIPT;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import live.lbtrip.domain.savedcourse.model.entity.SavedCourse;
import live.lbtrip.domain.savedcourse.model.entity.StoredImage;
import live.lbtrip.domain.savedcourse.repository.StoredImageRepository;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoredImageService {

    private final StoredImageRepository storedImageRepository;

    @Transactional
    public StoredImage registerReceipt(
        SavedCourse savedCourse,
        String storageKey,
        String contentType,
        long fileSize
    ) {
        StoredImage image = StoredImage.createReceipt(savedCourse, storageKey, contentType, fileSize);
        return storedImageRepository.save(image);
    }

    @Transactional
    public StoredImage claimReceipt(Long imageId, Long savedCourseId) {
        StoredImage image = storedImageRepository.findByIdAndSavedCourseIdAndPurpose(
                imageId,
                savedCourseId,
                RECEIPT
            )
            .orElseThrow(() -> BusinessException.of(ErrorCode.RECEIPT_IMAGE_NOT_FOUND));
        image.attach();
        return image;
    }
}
