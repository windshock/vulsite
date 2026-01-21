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

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model, HttpSession session) {
        if (session.getAttribute("user") == null) {
            return "redirect:/user/login";
        }
        Board board = boardService.getById(id);
        model.addAttribute("board", board);
        return "board/edit";
    }

    @PostMapping("/edit/{id}")
    public String edit(@PathVariable Long id,
                       @RequestParam String title,
                       @RequestParam String content) {
        boardService.update(id, title, content);
        return "redirect:/board/" + id;
    }

    /**
     * 취약점 #10: IDOR (타사용자 글 삭제)
     * 작성자 확인 없이 삭제 가능
     */
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        // 취약: 작성자 확인 없이 삭제
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
