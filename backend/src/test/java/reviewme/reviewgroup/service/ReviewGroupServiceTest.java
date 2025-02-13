package reviewme.reviewgroup.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static reviewme.fixture.ReviewGroupFixture.리뷰_그룹;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import reviewme.reviewgroup.repository.ReviewGroupRepository;
import reviewme.reviewgroup.service.dto.CheckValidAccessRequest;
import reviewme.reviewgroup.service.dto.CheckValidAccessResponse;
import reviewme.reviewgroup.service.dto.ReviewGroupCreationRequest;
import reviewme.reviewgroup.service.dto.ReviewGroupCreationResponse;
import reviewme.support.ServiceTest;

@ServiceTest
@ExtendWith(MockitoExtension.class)
class ReviewGroupServiceTest {

    @MockBean
    private RandomCodeGenerator randomCodeGenerator;

    @Autowired
    private ReviewGroupService reviewGroupService;

    @Autowired
    private ReviewGroupRepository reviewGroupRepository;

    @Test
    void 코드가_중복되는_경우_다시_생성한다() {
        // given
        reviewGroupRepository.save(리뷰_그룹("0000", "1111"));
        given(randomCodeGenerator.generate(anyInt()))
                .willReturn("0000") // ReviewRequestCode
                .willReturn("AAAA");

        ReviewGroupCreationRequest request = new ReviewGroupCreationRequest("sancho", "reviewme", "groupAccessCode");

        // when
        ReviewGroupCreationResponse response = reviewGroupService.createReviewGroup(request);

        // then
        assertThat(response).isEqualTo(new ReviewGroupCreationResponse("AAAA"));
        then(randomCodeGenerator).should(times(2)).generate(anyInt());
    }

    @Test
    void 리뷰_요청_코드와_리뷰_확인_코드가_일치하는지_확인한다() {
        // given
        String reviewRequestCode = "reviewRequestCode";
        String groupAccessCode = "groupAccessCode";
        reviewGroupRepository.save(리뷰_그룹(reviewRequestCode, groupAccessCode));

        CheckValidAccessRequest request = new CheckValidAccessRequest(reviewRequestCode, groupAccessCode);
        CheckValidAccessRequest wrongRequest = new CheckValidAccessRequest(reviewRequestCode, groupAccessCode + "!");

        // when
        CheckValidAccessResponse expected1 = reviewGroupService.checkGroupAccessCode(request);
        CheckValidAccessResponse expected2 = reviewGroupService.checkGroupAccessCode(wrongRequest);

        // then
        assertAll(
                () -> assertThat(expected1.hasAccess()).isTrue(),
                () -> assertThat(expected2.hasAccess()).isFalse()
        );
    }
}
