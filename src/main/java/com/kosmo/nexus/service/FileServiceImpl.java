package com.kosmo.nexus.service;

import com.kosmo.nexus.dto.FileDTO;
import com.kosmo.nexus.mapper.FileMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class FileServiceImpl implements FileService {

    private final FileMapper fileMapper;

    @Override
    public void saveFile(FileDTO fileDTO) {

        fileMapper.insertFile(fileDTO);
    }//-------------
    @Override
    public FileDTO getFileByBoardId(int boardId) {

        return fileMapper.selectFileByBoardId(boardId);
    }//--------------------
    @Override
    public void deleteFile(int fileId) {

        fileMapper.deleteFile(fileId);
    }

}
