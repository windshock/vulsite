package com.vulsite.service;

import com.vulsite.entity.Board;
import com.vulsite.entity.User;
import com.vulsite.repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * A담당 - 취약점 #2(Stored XSS), #10(IDOR) 구현
 */
@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;

    public List<Board> findAll() {
        return boardRepository.findAllByOrderByCreatedAtDesc();
    }

    public Optional<Board> findById(Long id) {
        eturn boardRepository.findById(id);
    }

    public Board getById(Long id) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));
        board.setViewCount(board.getViewCount() + 1);
        return boardRepository.save(board);
    }

    /**
     * 취약점 #2: Stored XSS (2차 수정)
     * 불완전한 필터링: script, img 태그 제거
     * 우회 가능: svg, body, iframe 등 다른 태그
     */
    public Board create(String title, String content, User author) {
        // 2차 수정: <script>, <img> 태그 필터링 (불완전)
        String filteredTitle = filterXss(title);
        String filteredContent = filterXss(content);

        Board board = new Board(filteredTitle, filteredContent, author);
        return boardRepository.save(board);
    }

    public Board update(Long id, String title, String content) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        // 2차 수정: <script>, <img> 태그 필터링 (불완전)
        String filteredTitle = filterXss(title);
        String filteredContent = filterXss(content);

        board.setTitle(filteredTitle);
        board.setContent(filteredContent);
        board.setUpdatedAt(LocalDateTime.now());
        return boardRepository.save(board);
    }

    /**
     * 2차 XSS 필터 (불완전)
     * script, img 태그만 제거 - svg, iframe 등 우회 가능
     */
    private String filterXss(String input) {
        if (input == null) return null;
        return input
                .replaceAll("<script>", "").replaceAll("</script>", "")
                .replaceAll("<img", "&lt;img");
    }

    /**
     * 취약점 #10: IDOR (타사용자 글 삭제)
     * 작성자 확인 없이 삭제
     */
    public void delete(Long id) {
        // TODO: A담당 - 현재 로그인 사용자와 작성자 비교 없이 삭제 (취약)
        boardRepository.deleteById(id);
    }

    public List<Board> search(String keyword) {
        return boardRepository.findByTitleContaining(keyword);
    }
}
