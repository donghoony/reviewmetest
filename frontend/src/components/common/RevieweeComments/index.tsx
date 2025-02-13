import * as S from './styles';

interface RevieweeCommentsProps {
  comment: string;
}

const DEFAULT_COMMENTS = '안녕하세요! 리뷰 잘 부탁드려요';

const RevieweeComments = ({ comment }: RevieweeCommentsProps) => {
  return <S.RevieweeComments>{comment || DEFAULT_COMMENTS}</S.RevieweeComments>;
};

export default RevieweeComments;
