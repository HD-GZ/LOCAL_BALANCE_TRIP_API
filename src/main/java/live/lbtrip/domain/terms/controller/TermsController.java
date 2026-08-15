package live.lbtrip.domain.terms.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import live.lbtrip.domain.terms.dto.response.TermsResponse;
import live.lbtrip.domain.terms.service.TermsService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/terms")
@RequiredArgsConstructor
public class TermsController implements TermsApi {

    private final TermsService termsService;

    @GetMapping("/{type}")
    public ResponseEntity<TermsResponse> getTerms(@PathVariable String type) {
        TermsResponse response = termsService.getTerms(type);
        return ResponseEntity.ok(response);
    }
}
