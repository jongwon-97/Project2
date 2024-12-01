package com.kosmo.nexus.service;

import com.kosmo.nexus.dto.FileDTO;
import com.kosmo.nexus.dto.ImageDTO;
import com.kosmo.nexus.mapper.FileMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final FileMapper fileMapper;
    private static final Logger log = LoggerFactory.getLogger(FileServiceImpl.class);

    @Override
    public void saveFile(FileDTO fileDTO) {
        fileMapper.insertFile(fileDTO);
    }

    @Override
    public FileDTO getFileByBoardId(int boardId) {
        return fileMapper.selectFileByBoardId(boardId);
    }

    @Override
    public void deleteFile(int fileId) {
        fileMapper.deleteFile(fileId);
    }

    @Override
    public List<FileDTO> getFilesByBoardId(int boardId) {
        return fileMapper.selectFilesByBoardId(boardId);
    }

    @Override
    public List<FileDTO> getFilesBySeasonId(int seasonId) {
        return fileMapper.selectFilesBySeasonId(seasonId); // FileMapper의 쿼리 호출
    }

    @Override
    public void deleteFilesBySeasonId(int seasonId) {
        fileMapper.deleteFilesBySeasonId(seasonId); // FileMapper의 쿼리 호출
    }

    @Override

    public void deleteFilesByBoardId(int boardId) {
        // 데이터베이스에서 파일 정보 가져오기
        List<FileDTO> files = fileMapper.selectFilesByBoardId(boardId);

        for (FileDTO file : files) {
            try {
                // 파일 시스템에서 파일 삭제
                Path path = Paths.get("src/main/resources/static" + file.getFilePath());
                Files.deleteIfExists(path); // 파일이 존재하면 삭제
                log.info("파일 삭제 성공: {}", path);
            } catch (Exception e) {
                log.error("파일 삭제 실패: {}", e.getMessage());
            }
        }

        // 데이터베이스에서 파일 정보 삭제
        fileMapper.deleteFilesByBoardId(boardId);
    }

    public void saveContent(ImageDTO imageDTO) {
        fileMapper.insertContent(imageDTO);
    }

    @Override
    public List<ImageDTO> getContentByBoardId(int boardId) {
        return fileMapper.selectContentByBoardId(boardId);
    }

    @Override
    public List<ImageDTO> getImagesByBoardId(int boardId) {
        return fileMapper.getImagesByBoardId(boardId);
    }

    @Override
    public void deleteImagesByBoardId(int boardId) {
        fileMapper.deleteImagesByBoardId(boardId);
    }

    @Override
    public List<ImageDTO> getTextsByBoardId(int boardId) {
        return fileMapper.getTextsByBoardId(boardId);
    }

    @Override
    public void deleteTextsByBoardId(int boardId) {
        fileMapper.deleteTextsByBoardId(boardId);
    }
    @Override
    public void deleteOnlyImagesByBoardId(int boardId) {
        fileMapper.deleteOnlyImagesByBoardId(boardId);
    }

    @Override
    public void deleteTextById(Integer textId) {
        fileMapper.deleteTextById(textId);
    }

    @Override
    public void deleteImageById(Integer imageId) {
        fileMapper.deleteImageById(imageId);
    }

}

