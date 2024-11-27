package com.kosmo.nexus.service;

import com.kosmo.nexus.dto.FileDTO;

import java.util.List;

public interface FileService {
    void saveFile(FileDTO fileDTO);
    FileDTO getFileByBoardId(int boardId);
    void deleteFile(int fileId);
    List<FileDTO> getFilesByBoardId(int boardId);

}
