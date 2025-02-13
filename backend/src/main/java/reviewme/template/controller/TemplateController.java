package reviewme.template.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reviewme.template.service.TemplateService;
import reviewme.template.service.dto.response.TemplateResponse;

@RestController
@RequiredArgsConstructor
public class TemplateController {

    private final TemplateService templateService;

    @GetMapping("/v2/reviews/write")
    public ResponseEntity<TemplateResponse> getReviewForm(@RequestParam String reviewRequestCode) {
        TemplateResponse response = templateService.generateReviewForm(reviewRequestCode);
        return ResponseEntity.ok(response);
    }
}
