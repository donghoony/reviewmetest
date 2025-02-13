import { ReviewDate } from '@/components';
import { ProjectImgProps } from '@/components/common/ProjectImg';
import { ReviewDateProps } from '@/components/common/ReviewDate';

// import LockToggle from '../LockToggle';

import * as S from './styles';

// const PROJECT_IMAGE_SIZE = '6rem';
const DATE_TITLE = '리뷰 작성일';

interface ReviewDescriptionProps extends Omit<ProjectImgProps, '$size'>, Omit<ReviewDateProps, 'dateTitle'> {
  isPublic: boolean;
  revieweeName: string;
  handleClickToggleButton: () => void;
}

const ReviewDescription = ({
  // thumbnailUrl,
  projectName,
  revieweeName,
  // isPublic,
  date,
  // handleClickToggleButton,
}: ReviewDescriptionProps) => {
  return (
    <S.Description>
      <S.DescriptionSide>
        {/* NOTE: 추후에 깃허브 로고 대신 다른 이미지로 대체될 수 있어서 일단 주석 처리 */}
        {/* <ProjectImg thumbnailUrl={thumbnailUrl} projectName={projectName} $size={PROJECT_IMAGE_SIZE} /> */}
        <S.ProjectInfoContainer>
          <S.ProjectName>{projectName}</S.ProjectName>
          <S.RevieweeNameAndDateContainer>
            <S.RevieweeNameWrapper>
              <S.RevieweeName>{revieweeName}</S.RevieweeName>에 대한 리뷰예요
            </S.RevieweeNameWrapper>
            <ReviewDate date={date} dateTitle={DATE_TITLE} />
          </S.RevieweeNameAndDateContainer>
        </S.ProjectInfoContainer>
      </S.DescriptionSide>
      {/* 시현 때 숨김 <S.DescriptionSide>
        <LockToggle $isPublic={isPublic} handleClickToggleButton={handleClickToggleButton} />
      </S.DescriptionSide> */}
    </S.Description>
  );
};

export default ReviewDescription;
