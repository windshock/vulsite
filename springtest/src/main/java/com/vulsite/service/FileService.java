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
     * 취약점 #3: File Upload (확장자/MIME 검증 없음)
     * 웹쉘 등 악성 파일 업로드 가능
     */
    public FileInfo upload(MultipartFile file, User uploader) throws IOException {
        // TODO: B담당 - 확장자, MIME 타입 검증 없이 업로드 허용 (취약)
        String originalName = file.getOriginalFilename();
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
     * 취약점 #4: Path Traversal (경로 검증 없음)
     * ../../etc/passwd 등으로 임의 파일 접근 가능
     */
    public Resource download(String filename) throws MalformedURLException {
        // TODO: B담당 - 경로 조작 검증 없이 파일 접근 허용 (취약)
        Path filePath = Paths.get(uploadDir).resolve(filename).normalize();
        Resource resource = new UrlResource(filePath.toUri());

        if (resource.exists()) {
            return resource;
        }
        throw new RuntimeException("파일을 찾을 수 없습니다: " + filename);
    }

    public List<FileInfo> findAll() {
        return fileRepository.findAll();
    }

    public Optional<FileInfo> findById(Long id) {
        return fileRepository.findById(id);
    }
}
