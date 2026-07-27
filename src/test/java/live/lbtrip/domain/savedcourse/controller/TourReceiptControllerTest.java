package live.lbtrip.domain.savedcourse.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import live.lbtrip.admin.auth.service.AdminJwtTokenProvider;
import live.lbtrip.domain.auth.model.JwtTokenSubject;
import live.lbtrip.domain.auth.service.JwtTokenProvider;
import live.lbtrip.domain.savedcourse.dto.response.ReceiptScanResponse;
import live.lbtrip.domain.savedcourse.dto.response.TourReceiptListResponse;
import live.lbtrip.domain.savedcourse.dto.response.TourReceiptResponse;
import live.lbtrip.domain.savedcourse.service.TourReceiptService;
import live.lbtrip.global.config.CorsProperties;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import live.lbtrip.support.fixture.AuthResponseFixture;
import live.lbtrip.support.fixture.TokenFixture;

@WebMvcTest(TourReceiptController.class)
@Import(TourReceiptControllerTest.TestCorsConfig.class)
class TourReceiptControllerTest {

    private static final Long SAVED_COURSE_ID = 1L;
    private static final Long IMAGE_ID = 2L;
    private static final Long RECEIPT_ID = 3L;
    private static final LocalDate PAID_DATE = LocalDate.of(2026, 7, 17);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TourReceiptService tourReceiptService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMappingContext;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private AdminJwtTokenProvider adminJwtTokenProvider;

    @Nested
    class 영수증_스캔 {

        @Test
        void OCR_실패_필드는_null로_응답한다() throws Exception {
            인증된_사용자();
            MockMultipartFile image = new MockMultipartFile(
                "image",
                "receipt.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF}
            );
            when(tourReceiptService.scan(
                org.mockito.ArgumentMatchers.eq(AuthResponseFixture.USER_ID),
                org.mockito.ArgumentMatchers.eq(SAVED_COURSE_ID),
                any()
            )).thenReturn(ReceiptScanResponse.of(
                IMAGE_ID,
                "https://images.example.com/receipt.jpg",
                live.lbtrip.domain.savedcourse.model.ReceiptOcrResult.empty()
            ));

            mockMvc.perform(multipart("/saved-courses/{savedCourseId}/receipts/scan", SAVED_COURSE_ID)
                    .file(image)
                    .header("Authorization", "Bearer " + TokenFixture.ACCESS_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.imageId").value(IMAGE_ID))
                .andExpect(jsonPath("$.data.merchantName").doesNotExist())
                .andExpect(jsonPath("$.data.amount").doesNotExist())
                .andExpect(jsonPath("$.data.paidDate").doesNotExist());
        }
    }

    @Nested
    class 증빙_등록 {

        @Test
        void 사용자가_확정한_필드로_증빙을_등록한다() throws Exception {
            인증된_사용자();
            when(tourReceiptService.create(
                org.mockito.ArgumentMatchers.eq(AuthResponseFixture.USER_ID),
                org.mockito.ArgumentMatchers.eq(SAVED_COURSE_ID),
                any()
            )).thenReturn(receiptResponse());

            mockMvc.perform(post("/saved-courses/{savedCourseId}/receipts", SAVED_COURSE_ID)
                    .header("Authorization", "Bearer " + TokenFixture.ACCESS_TOKEN)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "imageId": 2,
                          "merchantName": "국수거리 노포",
                          "amount": 18000,
                          "paidDate": "2026-07-17"
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.receiptId").value(RECEIPT_ID))
                .andExpect(jsonPath("$.data.merchantName").value("국수거리 노포"));
        }

        @Test
        void 이미지_식별자가_없으면_유효성_예외를_응답한다() throws Exception {
            인증된_사용자();

            mockMvc.perform(post("/saved-courses/{savedCourseId}/receipts", SAVED_COURSE_ID)
                    .header("Authorization", "Bearer " + TokenFixture.ACCESS_TOKEN)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "merchantName": "국수거리 노포",
                          "amount": 18000,
                          "paidDate": "2026-07-17"
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("INVALID_INPUT_VALUE"));
        }

        @Test
        void 사용할_수_없는_이미지면_예외를_응답한다() throws Exception {
            인증된_사용자();
            when(tourReceiptService.create(
                org.mockito.ArgumentMatchers.eq(AuthResponseFixture.USER_ID),
                org.mockito.ArgumentMatchers.eq(SAVED_COURSE_ID),
                any()
            )).thenThrow(BusinessException.of(ErrorCode.IMAGE_NOT_FOUND));

            mockMvc.perform(post("/saved-courses/{savedCourseId}/receipts", SAVED_COURSE_ID)
                    .header("Authorization", "Bearer " + TokenFixture.ACCESS_TOKEN)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "imageId": 2,
                          "merchantName": "국수거리 노포",
                          "amount": 18000,
                          "paidDate": "2026-07-17"
                        }
                        """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("IMAGE_NOT_FOUND"));
        }
    }

    @Nested
    class 증빙_조회와_삭제 {

        @Test
        void 증빙_목록을_조회한다() throws Exception {
            인증된_사용자();
            when(tourReceiptService.getReceipts(AuthResponseFixture.USER_ID, SAVED_COURSE_ID))
                .thenReturn(TourReceiptListResponse.of(18000, List.of()));

            mockMvc.perform(get("/saved-courses/{savedCourseId}/receipts", SAVED_COURSE_ID)
                    .header("Authorization", "Bearer " + TokenFixture.ACCESS_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.totalAmount").value(18000));
        }

        @Test
        void 증빙_상세를_조회한다() throws Exception {
            인증된_사용자();
            when(tourReceiptService.getReceipt(
                AuthResponseFixture.USER_ID,
                SAVED_COURSE_ID,
                RECEIPT_ID
            )).thenReturn(receiptResponse());

            mockMvc.perform(get(
                    "/saved-courses/{savedCourseId}/receipts/{receiptId}",
                    SAVED_COURSE_ID,
                    RECEIPT_ID
                )
                    .header("Authorization", "Bearer " + TokenFixture.ACCESS_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.receiptId").value(RECEIPT_ID));
        }

        @Test
        void 증빙을_삭제한다() throws Exception {
            인증된_사용자();

            mockMvc.perform(delete(
                    "/saved-courses/{savedCourseId}/receipts/{receiptId}",
                    SAVED_COURSE_ID,
                    RECEIPT_ID
                )
                    .header("Authorization", "Bearer " + TokenFixture.ACCESS_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"));

            verify(tourReceiptService).delete(
                AuthResponseFixture.USER_ID,
                SAVED_COURSE_ID,
                RECEIPT_ID
            );
        }
    }

    private TourReceiptResponse receiptResponse() {
        return TourReceiptResponse.of(
            RECEIPT_ID,
            "국수거리 노포",
            18000,
            PAID_DATE,
            "https://images.example.com/receipt.jpg"
        );
    }

    private void 인증된_사용자() {
        when(jwtTokenProvider.isValid(TokenFixture.ACCESS_TOKEN)).thenReturn(true);
        when(jwtTokenProvider.parseSubject(TokenFixture.ACCESS_TOKEN))
            .thenReturn(JwtTokenSubject.of(AuthResponseFixture.USER_ID));
    }

    @TestConfiguration
    static class TestCorsConfig {

        @Bean
        CorsProperties corsProperties() {
            return new CorsProperties(List.of("http://localhost"));
        }
    }
}
