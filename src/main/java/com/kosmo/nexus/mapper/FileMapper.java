package com.kosmo.nexus.mapper;

import com.kosmo.nexus.dto.FileDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FileMapper {
    void insertFile(FileDTO fileDTO);
    FileDTO selectFileByBoardId(@Param("boardId") int boardId);
    void deleteFile(@Param("fileId") int fileId);
    List<FileDTO> selectFilesByBoardId(@Param("boardId") int boardId);

}
