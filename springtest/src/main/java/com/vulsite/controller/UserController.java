package com.vulsite.controller;

import com.vulsite.entity.User;
import com.vulsite.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

/**
 * A담당 - 취약점 #1(SQL Injection), #9(IDOR) 구현
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    @GetMapping("/login")
    public String loginForm() {
        return "user/login";
    }

    /**
     * 취약점 #1: SQL Injection
     * 공격 예시: username에 ' OR '1'='1' -- 입력
     */
    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpSession session,
                        Model model) {
        User user = userService.loginVulnerable(username, password);
        if (user != null) {
            session.setAttribute("user", user);
            session.setAttribute("userId", user.getId());
            return "redirect:/";
        }
        model.addAttribute("error", "로그인 실패");
        return "user/login";
    }

    @GetMapping("/register")
    public String registerForm() {
        return "user/register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String username,
                           @RequestParam String password,
                           @RequestParam String email,
                           @RequestParam String name,
                           Model model) {
        try {
            userService.register(username, password, email, name);
            return "redirect:/user/login";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "user/register";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    @GetMapping("/profile/{id}")
    public String profile(@PathVariable Long id, Model model) {
        User user = userService.findById(id)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        model.addAttribute("profileUser", user);
        return "user/profile";
    }

    /**
     * 취약점 #9: IDOR (타사용자 정보 수정)
     * 권한 검증 없이 userId로 직접 수정 가능
     */
    @PostMapping("/update")
    public String updateUser(@RequestParam Long userId,
                             @RequestParam String email,
                             @RequestParam String name,
                             HttpSession session) {
        // 취약: 현재 로그인 사용자 확인 없이 수정
        userService.updateUser(userId, email, name);
        return "redirect:/user/profile/" + userId;
    }
}
