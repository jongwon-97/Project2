package com.kosmo.nexus.service;

import com.kosmo.nexus.dto.FileDTO;

import java.util.List;

public interface FileService {
    void saveFile(FileDTO fileDTO);
    FileDTO getFileByBoardId(int boardId);
    void deleteFile(int fileId);
    List<FileDTO> getFilesByBoardId(int boardId);

    List<FileDTO> getFilesBySeasonId(int seasonId); // 시즌 ID로 파일 데이터 조회
    void deleteFilesBySeasonId(int seasonId);       // 시즌 ID로 파일 데이터 삭제

}
