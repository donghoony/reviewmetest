package reviewme.template.service.mapper;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reviewme.question.domain.OptionGroup;
import reviewme.question.domain.OptionItem;
import reviewme.question.domain.Question;
import reviewme.question.repository.OptionGroupRepository;
import reviewme.question.repository.OptionItemRepository;
import reviewme.question.repository.QuestionRepository;
import reviewme.reviewgroup.domain.ReviewGroup;
import reviewme.template.domain.Section;
import reviewme.template.domain.SectionQuestion;
import reviewme.template.domain.Template;
import reviewme.template.domain.TemplateSection;
import reviewme.template.service.exception.MissingOptionItemsInOptionGroupException;
import reviewme.template.service.exception.SectionInTemplateNotFoundException;
import reviewme.template.service.exception.TemplateNotFoundByReviewGroupException;
import reviewme.template.repository.SectionRepository;
import reviewme.template.repository.TemplateRepository;
import reviewme.template.service.dto.response.OptionGroupResponse;
import reviewme.template.service.dto.response.OptionItemResponse;
import reviewme.template.service.dto.response.QuestionResponse;
import reviewme.template.service.dto.response.SectionResponse;
import reviewme.template.service.dto.response.TemplateResponse;
import reviewme.template.service.exception.QuestionInSectionNotFoundException;

@Component
@RequiredArgsConstructor
public class TemplateMapper {

    public static final String REVIEWEE_NAME_PLACEHOLDER = "{revieweeName}";

    private final TemplateRepository templateRepository;
    private final SectionRepository sectionRepository;
    private final QuestionRepository questionRepository;
    private final OptionGroupRepository optionGroupRepository;
    private final OptionItemRepository optionItemRepository;

    public TemplateResponse mapToTemplateResponse(ReviewGroup reviewGroup) {
        Template template = templateRepository.findById(reviewGroup.getTemplateId())
                .orElseThrow(() -> new TemplateNotFoundByReviewGroupException(
                        reviewGroup.getId(), reviewGroup.getTemplateId()
                ));

        List<SectionResponse> sectionResponses = template.getSectionIds()
                .stream()
                .map(templateSection -> mapToSectionResponse(templateSection, reviewGroup))
                .toList();

        return new TemplateResponse(
                template.getId(),
                reviewGroup.getReviewee(),
                reviewGroup.getProjectName(),
                sectionResponses
        );
    }

    private SectionResponse mapToSectionResponse(TemplateSection templateSection, ReviewGroup reviewGroup) {
        Section section = sectionRepository.findById(templateSection.getSectionId())
                .orElseThrow(() -> new SectionInTemplateNotFoundException(
                        templateSection.getTemplateId(), templateSection.getSectionId())
                );
        List<QuestionResponse> questionResponses = section.getQuestionIds()
                .stream()
                .map(sectionQuestion -> mapToQuestionResponse(sectionQuestion, reviewGroup))
                .toList();

        return new SectionResponse(
                section.getId(),
                section.getSectionName(),
                section.getVisibleType().name(),
                section.getOnSelectedOptionId(),
                section.convertHeader(REVIEWEE_NAME_PLACEHOLDER, reviewGroup.getReviewee()),
                questionResponses
        );
    }

    private QuestionResponse mapToQuestionResponse(SectionQuestion sectionQuestion, ReviewGroup reviewGroup) {
        Question question = questionRepository.findById(sectionQuestion.getQuestionId())
                .orElseThrow(() -> new QuestionInSectionNotFoundException(
                        sectionQuestion.getSectionId(), sectionQuestion.getQuestionId())
                );
        OptionGroupResponse optionGroupResponse = optionGroupRepository.findByQuestionId(question.getId())
                .map(this::mapToOptionGroupResponse)
                .orElse(null);

        return new QuestionResponse(
                question.getId(),
                question.isRequired(),
                question.convertContent(REVIEWEE_NAME_PLACEHOLDER, reviewGroup.getReviewee()),
                question.getQuestionType().name(),
                optionGroupResponse,
                question.hasGuideline(),
                question.convertGuideLine(REVIEWEE_NAME_PLACEHOLDER, reviewGroup.getReviewee())
        );
    }

    private OptionGroupResponse mapToOptionGroupResponse(OptionGroup optionGroup) {
        List<OptionItem> optionItems = optionItemRepository.findAllByOptionGroupId(optionGroup.getId());
        if (optionItems.isEmpty()) {
            throw new MissingOptionItemsInOptionGroupException(optionGroup.getId());
        }

        List<OptionItemResponse> optionItemResponses = optionItems.stream()
                .map(this::mapToOptionItemResponse)
                .toList();

        return new OptionGroupResponse(
                optionGroup.getId(),
                optionGroup.getMinSelectionCount(),
                optionGroup.getMaxSelectionCount(),
                optionItemResponses
        );
    }

    private OptionItemResponse mapToOptionItemResponse(OptionItem optionItem) {
        return new OptionItemResponse(optionItem.getId(), optionItem.getContent());
    }
}
