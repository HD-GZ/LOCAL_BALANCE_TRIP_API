package live.lbtrip.domain.savedcourse.controller;

import static org.springframework.http.HttpStatus.CREATED;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import live.lbtrip.domain.savedcourse.dto.request.TourReceiptCreateRequest;
import live.lbtrip.domain.savedcourse.dto.response.ReceiptScanResponse;
import live.lbtrip.domain.savedcourse.dto.response.TourReceiptListResponse;
import live.lbtrip.domain.savedcourse.dto.response.TourReceiptResponse;
import live.lbtrip.domain.savedcourse.service.TourReceiptService;
import live.lbtrip.global.web.UserId;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/saved-courses/{savedCourseId}/receipts")
@RequiredArgsConstructor
public class TourReceiptController implements TourReceiptApi {

    private final TourReceiptService tourReceiptService;

    @PostMapping(value = "/scan", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ReceiptScanResponse> scanReceipt(
        @UserId Long userId,
        @PathVariable Long savedCourseId,
        @RequestPart("image") MultipartFile image
    ) {
        ReceiptScanResponse response = tourReceiptService.scan(userId, savedCourseId, image);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<TourReceiptResponse> createReceipt(
        @UserId Long userId,
        @PathVariable Long savedCourseId,
        @Valid @RequestBody TourReceiptCreateRequest request
    ) {
        TourReceiptResponse response = tourReceiptService.create(userId, savedCourseId, request);
        return ResponseEntity.status(CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<TourReceiptListResponse> getReceipts(
        @UserId Long userId,
        @PathVariable Long savedCourseId
    ) {
        TourReceiptListResponse response = tourReceiptService.getReceipts(userId, savedCourseId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{receiptId}")
    public ResponseEntity<TourReceiptResponse> getReceipt(
        @UserId Long userId,
        @PathVariable Long savedCourseId,
        @PathVariable Long receiptId
    ) {
        TourReceiptResponse response = tourReceiptService.getReceipt(userId, savedCourseId, receiptId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{receiptId}")
    public ResponseEntity<Void> deleteReceipt(
        @UserId Long userId,
        @PathVariable Long savedCourseId,
        @PathVariable Long receiptId
    ) {
        tourReceiptService.delete(userId, savedCourseId, receiptId);
        return ResponseEntity.ok().build();
    }
}
