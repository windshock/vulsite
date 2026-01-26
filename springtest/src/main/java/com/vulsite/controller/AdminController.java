package com.vulsite.controller;

import com.vulsite.entity.User;
import com.vulsite.service.BoardService;
import com.vulsite.service.FileService;
import com.vulsite.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpSession;

/**
 * B담당 - 취약점 #8(관리자 페이지 직접 접근) 구현
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final BoardService boardService;
    private final FileService fileService;

    @Value("${admin.username}")
    private String adminUsername;

    @Value("${admin.password}")
    private String adminPassword;

    /**
     * 취약점 #8: 관리자 페이지 직접 접근 (2차 수정)
     * 불완전한 수정: dashboard, users만 관리자 확인
     * 우회 가능: /admin/config는 여전히 로그인만 확인
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session) {
        // 2차 수정: 관리자 여부 확인 (불완전 - /config는 미적용)
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/user/login";
        }
        if (!isAdmin(user)) {
            return "redirect:/?error=access_denied";
        }

        model.addAttribute("users", userService.findAll());
        model.addAttribute("boards", boardService.findAll());
        model.addAttribute("files", fileService.findAll());
        model.addAttribute("adminUsername", adminUsername);
        model.addAttribute("adminPassword", adminPassword);
        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String userList(Model model, HttpSession session) {
        // 2차 수정: 관리자 여부 확인
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/user/login";
        }
        if (!isAdmin(user)) {
            return "redirect:/?error=access_denied";
        }

        model.addAttribute("users", userService.findAll());
        return "admin/users";
    }

    @GetMapping("/config")
    public String config(Model model, HttpSession session) {
        // 2차 수정: 여전히 로그인만 확인 (불완전 - 관리자 확인 누락)
        // 취약점 #7: 평문 계정 정보 여전히 노출
        if (session.getAttribute("user") == null) {
            return "redirect:/user/login";
        }

        model.addAttribute("adminUsername", adminUsername);
        model.addAttribute("adminPassword", adminPassword);
        return "admin/config";
    }

    /**
     * 관리자 여부 확인 (username 기반)
     */
    private boolean isAdmin(User user) {
        return user != null && "admin".equals(user.getUsername());
    }
}
