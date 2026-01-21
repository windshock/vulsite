package com.vulsite.service;

import com.vulsite.entity.User;
import com.vulsite.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;

/**
 * A담당 - 취약점 #1(SQL Injection), #9(IDOR) 구현
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * 취약점 #1: SQL Injection
     * 사용자 입력값을 직접 쿼리에 삽입 (취약)
     */
    @SuppressWarnings("unchecked")
    public User loginVulnerable(String username, String password) {
        // TODO: A담당 - SQL Injection 취약 코드 구현
        // 예시: String query = "SELECT * FROM users WHERE username = '" + username + "' AND password = '" + password + "'";
        String query = "SELECT * FROM users WHERE username = '" + username + "' AND password = '" + password + "'";
        List<User> users = entityManager.createNativeQuery(query, User.class).getResultList();
        return users.isEmpty() ? null : users.get(0);
    }

    public User register(String username, String password, String email, String name) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("이미 존재하는 사용자명입니다.");
        }
        User user = new User(username, password, email, name);
        return userRepository.save(user);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * 취약점 #9: IDOR (타사용자 정보 수정)
     * 권한 검증 없이 userId로 직접 수정
     */
    public User updateUser(Long userId, String email, String name) {
        // TODO: A담당 - 현재 로그인 사용자와 userId 비교 없이 수정 허용 (취약)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        user.setEmail(email);
        user.setName(name);
        return userRepository.save(user);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }
}
