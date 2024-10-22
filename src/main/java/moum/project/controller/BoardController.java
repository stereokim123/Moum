package moum.project.controller;

import java.util.*;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import moum.project.service.BoardService;
import moum.project.service.StorageService;
import moum.project.vo.AttachedFile;
import moum.project.vo.Board;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/board")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;
    private final StorageService storageService;

    private final String folderName = "board/";

    @GetMapping({"/", "/boardHome"})
    public String boardHome(Model model) throws Exception {
        // 모든 게시글 리스트를 가져옴
        List<Board> allBoards = boardService.list();

        // 게시글 목록이 비어 있는 경우 기본 값을 설정
        if (allBoards.isEmpty()) {
            model.addAttribute("popularBoards", Collections.emptyList());
            model.addAttribute("recentBoards", Collections.emptyList());
            model.addAttribute("achievements", Collections.emptyList());
            return "board/boardHome";
        }

        // 예시로 인기글과 최근 게시글 리스트를 3개씩 추출 (임의의 로직)
        List<Board> popularBoards = allBoards.subList(0, Math.min(3, allBoards.size()));
        List<Board> recentBoards = allBoards.subList(0, Math.min(3, allBoards.size()));

        // 업적 랭킹 더미 데이터 설정
        List<Map<String, Object>> achievements = List.of(
                Map.of("rank", 1, "name", "레고 윈터랜드"),
                Map.of("rank", 2, "name", "대디하디"),
                Map.of("rank", 3, "name", "피규어 레이더")
        );

        // 모델에 데이터를 추가하여 뷰에 전달
        model.addAttribute("popularBoards", popularBoards);
        model.addAttribute("recentBoards", recentBoards);
        model.addAttribute("achievements", achievements);

        return "board/boardHome"; // boardHome.html 템플릿을 반환
    }

    @GetMapping({"/", "/boardList"})
    public String boardList(Model model) throws Exception {
        // 모든 게시글 리스트를 가져옴
        List<Board> allBoards = boardService.list();

        // 게시글 목록이 비어 있는 경우 기본 값을 설정
        if (allBoards.isEmpty()) {
            model.addAttribute("recentBoards", Collections.emptyList());
            return "board/boardList";
        }
        List<Board> recentBoards = allBoards.subList(0, Math.min(10, allBoards.size()));
        model.addAttribute("recentBoards", recentBoards);


        return "board/boardList";
    }

    // 게시글 생성
    @PostMapping("add")
    public String add(Board board, @RequestParam("files") MultipartFile[] files) throws Exception {
        // 임시로 사용자 번호 설정 (실제 구현 시에는 로그인 사용자 번호를 사용)
        board.setUserNo(1);

        // 파일 업로드 처리
        List<AttachedFile> attachedFiles = uploadFiles(files);

        // 업로드된 파일을 게시글에 추가
        board.setAttachedFiles(attachedFiles);

        // 게시글 저장
        boardService.add(board);
        return "redirect:/board/list";
    }

    // 게시글 목록 조회
    @GetMapping("list")
    public String list(Model model) throws Exception {
        List<Board> boardList = boardService.list();
        model.addAttribute("boardList", boardList);
        return "board/list";
    }

    // 게시글 상세 조회
    @GetMapping("view")
    public String view(@RequestParam("no") int no, Model model) throws Exception {
        Board board = boardService.get(no);
        model.addAttribute("board", board);
        return "board/view";
    }

    // 게시글 수정
    @PostMapping("update")
    public String update(Board board, @RequestParam("files") MultipartFile[] files) throws Exception {
        // 기존 게시글을 가져옴
        Board existingBoard = boardService.get(board.getNo());

        // 파일 업로드 처리
        List<AttachedFile> attachedFiles = uploadFiles(files);

        // 업로드된 파일을 기존 게시글에 추가
        existingBoard.setAttachedFiles(attachedFiles);
        existingBoard.setTitle(board.getTitle());
        existingBoard.setContent(board.getContent());

        // 게시글 업데이트
        boardService.update(existingBoard);
        return "redirect:/board/view?no=" + existingBoard.getNo();
    }

    // 게시글 삭제
    @GetMapping("delete")
    public String delete(@RequestParam("no") int no) throws Exception {
        boardService.delete(no);
        return "redirect:/board/list";
    }

    // 파일 업로드 처리 메서드
    private List<AttachedFile> uploadFiles(MultipartFile[] files) throws Exception {
        List<AttachedFile> attachedFiles = new ArrayList<>();

        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                continue; // 파일이 비어있으면 무시
            }

            // 새로운 첨부 파일 객체 생성
            AttachedFile attachedFile = new AttachedFile();
            attachedFile.setFileCategory(AttachedFile.BOARD);
            attachedFile.setFilename(UUID.randomUUID().toString());
            attachedFile.setOriginFilename(file.getOriginalFilename());

            // 파일 업로드 옵션 설정
            storageService.upload(folderName + attachedFile.getFilename(), file.getInputStream(),
                    Map.of(StorageService.CONTENT_TYPE, file.getContentType()));

            // 업로드된 파일 목록에 추가
            attachedFiles.add(attachedFile);
        }

        return attachedFiles;
    }
}
