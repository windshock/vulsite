package com.vulsite.controller;

import com.vulsite.entity.FileInfo;
import com.vulsite.entity.User;
import com.vulsite.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * B담당 - 취약점 #3(File Upload), #4(Path Traversal) 구현
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/file")
public class FileController {

    private final FileService fileService;

    @GetMapping
    public String list(Model model) {
        List<FileInfo> files = fileService.findAll();
        model.addAttribute("files", files);
        return "file/list";
    }

    @GetMapping("/upload")
    public String uploadForm(HttpSession session) {
        if (session.getAttribute("user") == null) {
            return "redirect:/user/login";
        }
        return "file/upload";
    }

    /**
     * 취약점 #3: File Upload
     * 확장자/MIME 타입 검증 없이 업로드 허용
     * 공격 예시: .jsp 웹쉘 업로드
     */
    @PostMapping("/upload")
    public String upload(@RequestParam("file") MultipartFile file,
                         HttpSession session,
                         Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/user/login";
        }

        try {
            // 취약: 파일 검증 없이 업로드
            fileService.upload(file, user);
            return "redirect:/file";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "file/upload";
        }
    }

    /**
     * 취약점 #4: Path Traversal
     * 경로 검증 없이 파일 다운로드
     * 공격 예시: ../../../etc/passwd
     */
    @GetMapping("/download")
    public ResponseEntity<Resource> download(@RequestParam String filename) {
        try {
            // 취약: 경로 조작 검증 없음
            Resource resource = fileService.download(filename);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + filename + "\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
