package com.kosmo.nexus.service;

import com.kosmo.nexus.dto.FileDTO;
import com.kosmo.nexus.mapper.FileMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final FileMapper fileMapper;

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

}
