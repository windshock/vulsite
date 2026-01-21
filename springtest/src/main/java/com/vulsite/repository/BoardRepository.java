package com.vulsite.repository;

import com.vulsite.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BoardRepository extends JpaRepository<Board, Long> {

    List<Board> findAllByOrderByCreatedAtDesc();

    List<Board> findByAuthorId(Long userId);

    List<Board> findByTitleContaining(String keyword);
}
