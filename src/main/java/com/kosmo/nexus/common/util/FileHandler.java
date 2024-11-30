package com.kosmo.nexus.common.util;

import jakarta.servlet.ServletContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Component
@Slf4j
public class FileHandler {

    @Autowired
    private ServletContext servletContext;

    // 이미지 저장 메서드
    public String saveImage(MultipartFile imageFile) throws IOException {
        String foldPath = "/member_img";
        Path path = Paths.get(servletContext.getRealPath(foldPath));
        if (!Files.exists(path)) {
            Files.createDirectories(path); // 경로가 없으면 디렉토리 생성
        }

        // 파일 이름 생성
        String originalFilename = imageFile.getOriginalFilename();
        String fileName = UUID.randomUUID().toString() + "_" + originalFilename;

        // 파일 저장
        Path destination = path.resolve(fileName);
        Files.copy(imageFile.getInputStream(), destination);

        // 저장된 파일 경로 반환
        return foldPath + "/" + fileName;
    }

    // 이미지 삭제 메서드
    public void deleteImage(String imagePath) throws IOException {
        Path path = Paths.get(servletContext.getRealPath(imagePath));
        if (Files.exists(path)) {
            Files.delete(path);
            log.info("이미지 파일 삭제됨: {}", imagePath);
        } else {
            log.warn("이미지 파일을 찾을 수 없음: {}", imagePath);
        }
    }
}
