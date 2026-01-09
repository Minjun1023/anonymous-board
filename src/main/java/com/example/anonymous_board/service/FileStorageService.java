package com.example.anonymous_board.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileStorageService {

    private final String UPLOAD_DIR = "uploads/";

    public String storeFile(MultipartFile file, String subDir) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("빈 파일은 저장할 수 없습니다.");
        }

        // 업로드 디렉토리가 없으면 생성
        Path uploadPath = Paths.get(UPLOAD_DIR + subDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // 파일 이름을 고유하게 생성
        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String newFilename = UUID.randomUUID().toString() + fileExtension;

        // 파일 저장
        Path filePath = uploadPath.resolve(newFilename);
        Files.copy(file.getInputStream(), filePath);

        // 웹에서 접근 가능한 경로 반환 (예: /uploads/posts/filename.jpg)
        // WebMvcConfig에서 리소스 핸들러 설정이 필요할 수 있음.
        // 현재 ProfileApiController에서는 /profiles/filename.jpg 로 반환하고 있음.
        // 여기서는 저장된 서브 디렉토리 경로를 포함하여 반환.
        return "/" + subDir + "/" + newFilename;
    }
}
