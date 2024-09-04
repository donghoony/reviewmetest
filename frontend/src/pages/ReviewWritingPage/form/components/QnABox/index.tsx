import { TEXT_ANSWER_LENGTH } from '@/pages/ReviewWritingPage/constants';
import { MultipleChoiceAnswer, TextAnswer } from '@/pages/ReviewWritingPage/form/components';
import { ReviewWritingCardQuestion } from '@/types';

import * as S from './style';

interface QnABoxProps {
  question: ReviewWritingCardQuestion;
}
/**
 * 하나의 질문과 그에 대한 답을 관리
 */

const QnABox = ({ question }: QnABoxProps) => {
  /**
   * 객관식 문항의 최소,최대 개수에 대한 안내 문구
   */
  const multipleLGuideline = (() => {
    const { optionGroup, questionType } = question;

    // NOTE: 객관식일 경우의 안내 문구 처리
    if (questionType === 'TEXT') {
      const guideline = question.required
        ? `(최소 ${TEXT_ANSWER_LENGTH.min}자 ~ 최대 ${TEXT_ANSWER_LENGTH.max}자)`
        : `(최대 ${TEXT_ANSWER_LENGTH.max}자)`;
      return guideline;
    }

    if (!optionGroup) return;
    const { minCount, maxCount } = optionGroup;

    const isAllSelectAvailable = maxCount === optionGroup.options.length;

    if (!maxCount || isAllSelectAvailable) return `(최소 ${minCount}개 이상)`;

    return `(${minCount}개 ~ ${maxCount}개)`;
  })();

  return (
    <S.QnASection>
      <S.QuestionTitle>
        {question.content}
        {question.required ? <S.QuestionRequiredMark>*</S.QuestionRequiredMark> : <span> (선택) </span>}
        <S.MultipleGuideline>{multipleLGuideline ?? ''}</S.MultipleGuideline>
      </S.QuestionTitle>
      {question.guideline && <S.QuestionGuideline>{question.guideline}</S.QuestionGuideline>}
      {/*객관식*/}
      {question.questionType === 'CHECKBOX' && <MultipleChoiceAnswer question={question} />}
      {/*서술형*/}
      {question.questionType === 'TEXT' && <TextAnswer question={question} />}
    </S.QnASection>
  );
};

export default QnABox;
