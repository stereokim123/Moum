package moum.project.dao;

import moum.project.vo.Board;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface BoardDao {
    List<Board> list(); // 모든 게시글 조회

    Board selectById(int boardId); // 특정 게시글 조회

    void insert(Board board); // 새 게시글 생성

    int update(Board board); // 게시글 수정

    int delete(int boardId); // 논리적 삭제

    int increaseViewCount(int boardId); // 조회수 증가

    List<Board> selectByUserId(int userId); // 특정 사용자의 게시글 조회

    List<Board> selectRecent(int limit); // 최근 게시글 조회

    // 전체 게시글 조회
    List<Board> listAll();

    // 인기 게시글 조회
    List<Board> listPopular();

    // 수집품 거래 게시글 조회
    List<Board> listTradePosts(int limit);

    // 자랑하기 게시글 조회
    List<Board> listBraggingPosts();
}
