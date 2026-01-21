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
     * 취약점 #8: 관리자 페이지 직접 접근
     * 인증/권한 체크 없이 접근 가능
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // 취약: 인증 체크 없음
        model.addAttribute("users", userService.findAll());
        model.addAttribute("boards", boardService.findAll());
        model.addAttribute("files", fileService.findAll());
        model.addAttribute("adminUsername", adminUsername);
        model.addAttribute("adminPassword", adminPassword);
        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String userList(Model model) {
        // 취약: 인증 체크 없음
        model.addAttribute("users", userService.findAll());
        return "admin/users";
    }

    @GetMapping("/config")
    public String config(Model model) {
        // 취약점 #7: 평문 계정 정보 노출
        model.addAttribute("adminUsername", adminUsername);
        model.addAttribute("adminPassword", adminPassword);
        return "admin/config";
    }
}
