package com.vulsite.controller;

import com.vulsite.entity.Board;
import com.vulsite.entity.User;
import com.vulsite.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * A담당 - 취약점 #2(Stored XSS), #10(IDOR) 구현
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/board")
public class BoardController {

    private final BoardService boardService;

    @GetMapping
    public String list(Model model) {
        List<Board> boards = boardService.findAll();
        model.addAttribute("boards", boards);
        return "board/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Board board = boardService.getById(id);
        model.addAttribute("board", board);
        return "board/detail";
    }

    @GetMapping("/write")
    public String writeForm(HttpSession session) {
        if (session.getAttribute("user") == null) {
            return "redirect:/user/login";
        }
        return "board/write";
    }

    /**
     * 취약점 #2: Stored XSS
     * 사용자 입력을 필터링 없이 저장
     * 공격 예시: <script>alert('XSS')</script>
     */
    @PostMapping("/write")
    public String write(@RequestParam String title,
                        @RequestParam String content,
                        HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/user/login";
        }
        // 취약: XSS 필터링 없이 저장
        boardService.create(title, content, user);
        return "redirect:/board";
    }

    /**
     * IDOR 2차 수정: 수정/삭제 모두 작성자 확인
     * 불완전: GET 요청(조회)에서는 검증 없음 - 타인 글 조회 가능
     */
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/user/login";
        }

        Board board = boardService.getById(id);

        // 2차 수정: 수정 폼 접근 시 작성자 확인
        if (!board.getAuthor().getId().equals(user.getId())) {
            return "redirect:/board?error=unauthorized";
        }

        model.addAttribute("board", board);
        return "board/edit";
    }

    @PostMapping("/edit/{id}")
    public String edit(@PathVariable Long id,
                       @RequestParam String title,
                       @RequestParam String content,
                       HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/user/login";
        }

        Board board = boardService.getById(id);

        // 2차 수정: 수정 처리 시 작성자 확인
        if (!board.getAuthor().getId().equals(user.getId())) {
            return "redirect:/board?error=unauthorized";
        }

        boardService.update(id, title, content);
        return "redirect:/board/" + id;
    }

    /**
     * IDOR 2차 수정: 삭제에도 작성자 확인 추가
     * 불완전: POST 요청만 검증, HTTP Method 변경 시 우회 가능성
     */
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/user/login";
        }

        Board board = boardService.getById(id);

        // 2차 수정: 삭제 시 작성자 확인 (불완전 - 조회는 여전히 미검증)
        if (!board.getAuthor().getId().equals(user.getId())) {
            return "redirect:/board?error=unauthorized";
        }

        boardService.delete(id);
        return "redirect:/board";
    }

    @GetMapping("/search")
    public String search(@RequestParam String keyword, Model model) {
        List<Board> boards = boardService.search(keyword);
        model.addAttribute("boards", boards);
        model.addAttribute("keyword", keyword);
        return "board/list";
    }
}
