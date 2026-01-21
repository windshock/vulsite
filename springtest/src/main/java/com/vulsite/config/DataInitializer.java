package com.vulsite.config;

import com.vulsite.entity.Board;
import com.vulsite.entity.User;
import com.vulsite.repository.BoardRepository;
import com.vulsite.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final BoardRepository boardRepository;

    @Override
    public void run(String... args) {
        // 테스트 사용자 생성
        User admin = new User("admin", "admin123", "admin@vulsite.com", "관리자");
        admin.setAdmin(true);
        userRepository.save(admin);

        User user1 = new User("user1", "password1", "user1@vulsite.com", "사용자1");
        userRepository.save(user1);

        User user2 = new User("user2", "password2", "user2@vulsite.com", "사용자2");
        userRepository.save(user2);

        // 테스트 게시글 생성
        boardRepository.save(new Board("환영합니다!", "Vulsite에 오신 것을 환영합니다.\n이 사이트는 취약점 학습을 위한 테스트 환경입니다.", admin));
        boardRepository.save(new Board("SQL Injection 테스트", "로그인 페이지에서 SQL Injection을 테스트해보세요.\n예시: ' OR '1'='1' --", user1));
        boardRepository.save(new Board("XSS 테스트", "이 게시판에서 Stored XSS를 테스트해보세요.\n예시: <script>alert('XSS')</script>", user2));

        System.out.println("===========================================");
        System.out.println("  Vulsite 초기 데이터 생성 완료!");
        System.out.println("===========================================");
        System.out.println("  테스트 계정:");
        System.out.println("  - admin / admin123 (관리자)");
        System.out.println("  - user1 / password1");
        System.out.println("  - user2 / password2");
        System.out.println("===========================================");
    }
}
