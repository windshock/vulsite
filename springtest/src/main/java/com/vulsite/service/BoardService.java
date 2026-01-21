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
        return boardRepository.findById(id);
    }

    public Board getById(Long id) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));
        board.setViewCount(board.getViewCount() + 1);
        return boardRepository.save(board);
    }

    /**
     * 취약점 #2: Stored XSS
     * 사용자 입력 (title, content)을 이스케이프 없이 저장
     */
    public Board create(String title, String content, User author) {
        // TODO: A담당 - XSS 필터링 없이 저장 (취약)
        Board board = new Board(title, content, author);
        return boardRepository.save(board);
    }

    public Board update(Long id, String title, String content) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));
        board.setTitle(title);
        board.setContent(content);
        board.setUpdatedAt(LocalDateTime.now());
        return boardRepository.save(board);
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
