package com.vulsite.repository;

import com.vulsite.entity.FileInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileRepository extends JpaRepository<FileInfo, Long> {

    List<FileInfo> findByUploaderId(Long userId);

    Optional<FileInfo> findByStoredName(String storedName);
}
