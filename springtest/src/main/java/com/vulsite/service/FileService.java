package com.vulsite.service;

import com.vulsite.entity.FileInfo;
import com.vulsite.entity.User;
import com.vulsite.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

/**
 * B담당 - 취약점 #3(File Upload), #4(Path Traversal) 구현
 */
@Service
@RequiredArgsConstructor
public class FileService {

    private final FileRepository fileRepository;

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    /**
     * 취약점 #3: File Upload (2차 수정)
     * 불완전한 필터링: JSP 관련 확장자 블랙리스트
     * 우회 가능: 대소문자 (.JsP, .JSP)
     */
    public FileInfo upload(MultipartFile file, User uploader) throws IOException {
        String originalName = file.getOriginalFilename();

        // 2차 수정: JSP 관련 확장자 블랙리스트 (불완전 - 대소문자 미처리)
        if (originalName != null) {
            String[] blockedExtensions = {".jsp", ".jspx", ".jspa", ".jspf"};
            for (String ext : blockedExtensions) {
                if (originalName.endsWith(ext)) {
                    throw new IOException("JSP 관련 파일은 업로드할 수 없습니다.");
                }
            }
        }

        String storedName = System.currentTimeMillis() + "_" + originalName;
        String filePath = uploadDir + "/" + storedName;

        // 디렉토리 생성
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 파일 저장 (검증 없음 - 취약)
        file.transferTo(new File(filePath));

        FileInfo fileInfo = new FileInfo(
                originalName,
                storedName,
                filePath,
                file.getSize(),
                file.getContentType(),
                uploader
        );

        return fileRepository.save(fileInfo);
    }

    /**
     * 취약점 #4: Path Traversal (2차 수정)
     * 불완전한 필터링: ../ 재귀적 제거
     * 우회 가능: URL 인코딩 (..%2f, %2e%2e/)
     */
    public Resource download(String filename) throws MalformedURLException {
        // 2차 수정: ../ 재귀적 제거 (불완전 - URL 인코딩 미처리)
        String sanitizedFilename = removePathTraversal(filename);

        Path filePath = Paths.get(uploadDir).resolve(sanitizedFilename).normalize();
        Resource resource = new UrlResource(filePath.toUri());

        if (resource.exists()) {
            return resource;
        }
        throw new RuntimeException("파일을 찾을 수 없습니다: " + sanitizedFilename);
    }

    /**
     * 2차 Path Traversal 필터 (불완전)
     * ../ 재귀 제거 - URL 인코딩 우회 가능
     */
    private String removePathTraversal(String filename) {
        String result = filename;
        while (result.contains("../")) {
            result = result.replace("../", "");
        }
        return result;
    }

    public List<FileInfo> findAll() {
        return fileRepository.findAll();
    }

    public Optional<FileInfo> findById(Long id) {
        return fileRepository.findById(id);
    }
}
