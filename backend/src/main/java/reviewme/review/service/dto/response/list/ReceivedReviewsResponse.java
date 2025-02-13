package reviewme.review.service.dto.response.list;

import java.util.List;

public record ReceivedReviewsResponse(
        String revieweeName,
        String projectName,
        List<ReviewListElementResponse> reviews
) {
}
