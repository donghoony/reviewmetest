package reviewme.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.restdocs.headers.HeaderDescriptor;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.request.ParameterDescriptor;
import reviewme.review.domain.exception.ReviewGroupNotFoundByReviewRequestCodeException;
import reviewme.review.service.dto.request.ReviewRegisterRequest;
import reviewme.review.service.dto.response.ReviewRequestTokenResponse;
import reviewme.review.service.dto.response.list.ReviewCategoryResponse;
import reviewme.review.service.dto.response.list.ReviewListElementResponse;
import reviewme.review.service.dto.response.list.ReceivedReviewsResponse;
import reviewme.review.service.exception.ReviewGroupNotFoundByCodesException;

class ReviewApiTest extends ApiTest {

    private final String request = """
            {
                "reviewRequestCode": "ABCD1234",
                "answers": [
                    {
                        "questionId": 1,
                        "selectedOptionIds": [1, 2]
                    },
                    {
                        "questionId": 2,
                        "text": "답변 예시 1"
                    }
                ]
            }
            """;

    @Test
    void 리뷰를_등록한다() {
        BDDMockito.given(reviewRegisterService.registerReview(any(ReviewRegisterRequest.class)))
                .willReturn(1L);

        FieldDescriptor[] requestFieldDescriptors = {
                fieldWithPath("reviewRequestCode").description("리뷰 요청 코드"),

                fieldWithPath("answers[]").description("답변 목록"),
                fieldWithPath("answers[].questionId").description("질문 ID"),
                fieldWithPath("answers[].selectedOptionIds").description("선택한 옵션 ID 목록").optional(),
                fieldWithPath("answers[].text").description("서술 답변").optional()
        };

        RestDocumentationResultHandler handler = document(
                "create-review",
                requestFields(requestFieldDescriptors)
        );

        givenWithSpec().log().all()
                .body(request)
                .when().post("/v2/reviews")
                .then().log().all()
                .apply(handler)
                .statusCode(201);
    }

    @Test
    void 리뷰_그룹_코드가_올바르지_않은_경우_예외가_발생한다() {
        BDDMockito.given(reviewRegisterService.registerReview(any(ReviewRegisterRequest.class)))
                .willThrow(new ReviewGroupNotFoundByReviewRequestCodeException(anyString()));

        FieldDescriptor[] requestFieldDescriptors = {
                fieldWithPath("reviewRequestCode").description("리뷰 요청 코드"),

                fieldWithPath("answers[]").description("답변 목록"),
                fieldWithPath("answers[].questionId").description("질문 ID"),
                fieldWithPath("answers[].selectedOptionIds").description("선택한 옵션 ID 목록").optional(),
                fieldWithPath("answers[].text").description("서술형 답변").optional()
        };

        RestDocumentationResultHandler handler = document(
                "create-review-invalid-review-request-code",
                requestFields(requestFieldDescriptors)
        );

        givenWithSpec().log().all()
                .body(request)
                .when().post("/v2/reviews")
                .then().log().all()
                .apply(handler)
                .statusCode(404);
    }

    @Deprecated(since = "1.0.2", forRemoval = true)
    @Test
    void 자신이_받은_리뷰_한_개를_조회한다() {
        BDDMockito.given(reviewDetailLookupService.getReviewDetail(anyLong(), anyString(), anyString()))
                .willReturn(TemplateFixture.templateAnswerResponse());

        HeaderDescriptor[] requestHeaderDescriptors = {
                headerWithName("groupAccessCode").description("그룹 접근 코드")
        };

        ParameterDescriptor[] requestPathDescriptors = {
                parameterWithName("id").description("리뷰 ID")
        };

        FieldDescriptor[] responseFieldDescriptors = {
                fieldWithPath("createdAt").description("리뷰 작성 날짜"),
                fieldWithPath("formId").description("폼 ID"),
                fieldWithPath("revieweeName").description("리뷰이 이름"),
                fieldWithPath("projectName").description("프로젝트 이름"),

                fieldWithPath("sections[]").description("섹션 목록"),
                fieldWithPath("sections[].sectionId").description("섹션 ID"),
                fieldWithPath("sections[].header").description("섹션 제목"),

                fieldWithPath("sections[].questions[]").description("질문 목록"),
                fieldWithPath("sections[].questions[].questionId").description("질문 ID"),
                fieldWithPath("sections[].questions[].required").description("필수 여부"),
                fieldWithPath("sections[].questions[].content").description("질문 내용"),
                fieldWithPath("sections[].questions[].questionType").description("질문 타입"),

                fieldWithPath("sections[].questions[].optionGroup").description("옵션 그룹").optional(),
                fieldWithPath("sections[].questions[].optionGroup.optionGroupId").description("옵션 그룹 ID"),
                fieldWithPath("sections[].questions[].optionGroup.minCount").description("최소 선택 개수"),
                fieldWithPath("sections[].questions[].optionGroup.maxCount").description("최대 선택 개수"),

                fieldWithPath("sections[].questions[].optionGroup.options[]").description("선택 항목 목록"),
                fieldWithPath("sections[].questions[].optionGroup.options[].optionId").description("선택 항목 ID"),
                fieldWithPath("sections[].questions[].optionGroup.options[].content").description("선택 항목 내용"),
                fieldWithPath("sections[].questions[].optionGroup.options[].isChecked").description("선택 여부"),
                fieldWithPath("sections[].questions[].answer").description("서술형 답변").optional(),
        };

        RestDocumentationResultHandler handler = document(
                "review-detail",
                requestHeaders(requestHeaderDescriptors),
                pathParameters(requestPathDescriptors),
                responseFields(responseFieldDescriptors)
        );

        givenWithSpec().log().all()
                .pathParam("id", "1")
                .queryParam("reviewRequestCode", "00001234")
                .header("groupAccessCode", "abc12344")
                .when().get("/v2/reviews/{id}")
                .then().log().all()
                .apply(handler)
                .statusCode(200);
    }

    @Test
    void 토큰으로_자신이_받은_리뷰_한_개를_조회한다() {
        BDDMockito.given(reviewDetailLookupService.getReviewDetail2(anyLong(), anyString()))
                .willReturn(TemplateFixture.templateAnswerResponse());

        HeaderDescriptor[] requestHeaderDescriptors = {
                headerWithName("Authorization").description("그룹 접근 토큰")
        };

        ParameterDescriptor[] requestPathDescriptors = {
                parameterWithName("id").description("리뷰 ID")
        };

        FieldDescriptor[] responseFieldDescriptors = {
                fieldWithPath("createdAt").description("리뷰 작성 날짜"),
                fieldWithPath("formId").description("폼 ID"),
                fieldWithPath("revieweeName").description("리뷰이 이름"),
                fieldWithPath("projectName").description("프로젝트 이름"),

                fieldWithPath("sections[]").description("섹션 목록"),
                fieldWithPath("sections[].sectionId").description("섹션 ID"),
                fieldWithPath("sections[].header").description("섹션 제목"),

                fieldWithPath("sections[].questions[]").description("질문 목록"),
                fieldWithPath("sections[].questions[].questionId").description("질문 ID"),
                fieldWithPath("sections[].questions[].required").description("필수 여부"),
                fieldWithPath("sections[].questions[].content").description("질문 내용"),
                fieldWithPath("sections[].questions[].questionType").description("질문 타입"),

                fieldWithPath("sections[].questions[].optionGroup").description("옵션 그룹").optional(),
                fieldWithPath("sections[].questions[].optionGroup.optionGroupId").description("옵션 그룹 ID"),
                fieldWithPath("sections[].questions[].optionGroup.minCount").description("최소 선택 개수"),
                fieldWithPath("sections[].questions[].optionGroup.maxCount").description("최대 선택 개수"),

                fieldWithPath("sections[].questions[].optionGroup.options[]").description("선택 항목 목록"),
                fieldWithPath("sections[].questions[].optionGroup.options[].optionId").description("선택 항목 ID"),
                fieldWithPath("sections[].questions[].optionGroup.options[].content").description("선택 항목 내용"),
                fieldWithPath("sections[].questions[].optionGroup.options[].isChecked").description("선택 여부"),
                fieldWithPath("sections[].questions[].answer").description("서술형 답변").optional(),
        };

        RestDocumentationResultHandler handler = document(
                "review-detail-with-token",
                requestHeaders(requestHeaderDescriptors),
                pathParameters(requestPathDescriptors),
                responseFields(responseFieldDescriptors)
        );

        givenWithSpec().log().all()
                .pathParam("id", "1")
                .header("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c")
                .when().get("/vx/reviews/{id}")
                .then().log().all()
                .apply(handler)
                .statusCode(200);
    }

    @Test
    void 리뷰_단건_조회시_접근_코드가_올바르지_않은_경우_예외를_발생한다() {
        long reviewId = 1L;
        String reviewRequestCode = "00001234";
        String groupAccessCode = "43214321";
        BDDMockito.given(reviewDetailLookupService.getReviewDetail(reviewId, reviewRequestCode, groupAccessCode))
                .willThrow(new ReviewGroupNotFoundByCodesException(reviewRequestCode, groupAccessCode));

        HeaderDescriptor[] requestHeaderDescriptors = {
                headerWithName("groupAccessCode").description("그룹 접근 코드")
        };

        ParameterDescriptor[] requestPathDescriptors = {
                parameterWithName("id").description("리뷰 ID")
        };

        RestDocumentationResultHandler handler = document(
                "review-detail-invalid-group-access-code",
                requestHeaders(requestHeaderDescriptors),
                pathParameters(requestPathDescriptors)
        );

        givenWithSpec().log().all()
                .pathParam("id", reviewId)
                .queryParam("reviewRequestCode", reviewRequestCode)
                .header("groupAccessCode", groupAccessCode)
                .when().get("/v2/reviews/{id}")
                .then().log().all()
                .apply(handler)
                .statusCode(400);
    }

    @Deprecated(since = "1.0.2", forRemoval = true)
    @Test
    void 자신이_받은_리뷰_목록을_조회한다() {
        List<ReviewListElementResponse> receivedReviews = List.of(
                new ReviewListElementResponse(1L, LocalDate.of(2024, 8, 1), "(리뷰 미리보기 1)",
                        List.of(new ReviewCategoryResponse(1L, "카테고리 1"))),
                new ReviewListElementResponse(2L, LocalDate.of(2024, 8, 2), "(리뷰 미리보기 2)",
                        List.of(new ReviewCategoryResponse(2L, "카테고리 2")))
        );
        ReceivedReviewsResponse response = new ReceivedReviewsResponse("아루", "리뷰미", receivedReviews);
        BDDMockito.given(reviewListLookupService.getReceivedReviews(anyString(), anyString()))
                .willReturn(response);

        HeaderDescriptor[] requestHeaderDescriptors = {
                headerWithName("groupAccessCode").description("그룹 접근 코드")
        };

        FieldDescriptor[] responseFieldDescriptors = {
                fieldWithPath("revieweeName").description("리뷰이 이름"),
                fieldWithPath("projectName").description("프로젝트 이름"),

                fieldWithPath("reviews[]").description("리뷰 목록"),
                fieldWithPath("reviews[].reviewId").description("리뷰 ID"),
                fieldWithPath("reviews[].createdAt").description("리뷰 작성 날짜"),
                fieldWithPath("reviews[].contentPreview").description("리뷰 미리보기"),

                fieldWithPath("reviews[].categories[]").description("카테고리 목록"),
                fieldWithPath("reviews[].categories[].optionId").description("카테고리 ID"),
                fieldWithPath("reviews[].categories[].content").description("카테고리 내용")
        };

        RestDocumentationResultHandler handler = document(
                "received-reviews",
                requestHeaders(requestHeaderDescriptors),
                responseFields(responseFieldDescriptors)
        );

        givenWithSpec().log().all()
                .queryParam("reviewRequestCode", "asdfasdf")
                .header("groupAccessCode", "qwerqwer")
                .when().get("/v2/reviews")
                .then().log().all()
                .apply(handler)
                .statusCode(200);
    }

    @Test
    void 토큰으로_자신이_받은_리뷰_목록을_조회한다() {
        List<ReviewListElementResponse> receivedReviews = List.of(
                new ReviewListElementResponse(1L, LocalDate.of(2024, 8, 1), "(리뷰 미리보기 1)",
                        List.of(new ReviewCategoryResponse(1L, "카테고리 1"))),
                new ReviewListElementResponse(2L, LocalDate.of(2024, 8, 2), "(리뷰 미리보기 2)",
                        List.of(new ReviewCategoryResponse(2L, "카테고리 2")))
        );
        ReceivedReviewsResponse response = new ReceivedReviewsResponse("아루", "리뷰미", receivedReviews);
        BDDMockito.given(reviewListLookupService.getReceivedReviews2(anyString()))
                .willReturn(response);

        HeaderDescriptor[] requestHeaderDescriptors = {
                headerWithName("Authorization").description("그룹 접근 토큰")
        };

        FieldDescriptor[] responseFieldDescriptors = {
                fieldWithPath("revieweeName").description("리뷰이 이름"),
                fieldWithPath("projectName").description("프로젝트 이름"),

                fieldWithPath("reviews[]").description("리뷰 목록"),
                fieldWithPath("reviews[].reviewId").description("리뷰 ID"),
                fieldWithPath("reviews[].createdAt").description("리뷰 작성 날짜"),
                fieldWithPath("reviews[].contentPreview").description("리뷰 미리보기"),

                fieldWithPath("reviews[].categories[]").description("카테고리 목록"),
                fieldWithPath("reviews[].categories[].optionId").description("카테고리 ID"),
                fieldWithPath("reviews[].categories[].content").description("카테고리 내용")
        };

        RestDocumentationResultHandler handler = document(
                "received-reviews-with-token",
                requestHeaders(requestHeaderDescriptors),
                responseFields(responseFieldDescriptors)
        );

        givenWithSpec().log().all()
                .header("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c")
                .when().get("/vx/reviews")
                .then().log().all()
                .apply(handler)
                .statusCode(200);
    }

    @Test
    void 자신이_받은_리뷰_조회시_접근_코드가_올바르지_않은_경우_예외를_발생한다() {
        String reviewRequestCode = "43214321";
        String groupAccessCode = "00001234";
        BDDMockito.given(reviewListLookupService.getReceivedReviews(reviewRequestCode, groupAccessCode))
                .willThrow(new ReviewGroupNotFoundByCodesException(reviewRequestCode, groupAccessCode));

        HeaderDescriptor[] requestHeaderDescriptors = {
                headerWithName("groupAccessCode").description("그룹 접근 코드")
        };

        RestDocumentationResultHandler handler = document(
                "received-reviews-invalid-group-access-code",
                requestHeaders(requestHeaderDescriptors)
        );

        givenWithSpec().log().all()
                .header("groupAccessCode", groupAccessCode)
                .queryParam("reviewRequestCode", reviewRequestCode)
                .when().get("/v2/reviews")
                .then().log().all()
                .apply(handler)
                .statusCode(400);
    }

    @Test
    void 리뷰_토큰을_발급한다() {
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
        String body = """
                {
                    "reviewRequestCode": "ABCD1234",
                    "groupAccessCode": "12341234"
                }
                """;
        BDDMockito.given(reviewTokenService.generateToken(any()))
                .willReturn(new ReviewRequestTokenResponse(token));

        FieldDescriptor[] requestFieldDescriptors = {
                fieldWithPath("reviewRequestCode").description("리뷰 요청 코드"),
                fieldWithPath("groupAccessCode").description("비밀번호")
        };

        FieldDescriptor[] responseFieldDescriptors = {
                fieldWithPath("reviewRequestToken").description("리뷰 토큰")
        };

        RestDocumentationResultHandler handler = document(
                "create-review-token",
                requestFields(requestFieldDescriptors),
                responseFields(responseFieldDescriptors)
        );

        givenWithSpec().log().all()
                .body(body)
                .when().post("/vx/token")
                .then().log().all()
                .apply(handler)
                .statusCode(200);
    }
}
