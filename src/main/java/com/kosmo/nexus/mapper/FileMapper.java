package com.kosmo.nexus.mapper;

import com.kosmo.nexus.dto.FileDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface FileMapper {
    void insertFile(FileDTO fileDTO); // 파일 저장

    FileDTO selectFileByBoardId(int boardId); // 특정 게시판 ID의 파일 조회

    List<FileDTO> selectFilesByBoardId(int boardId); // 특정 게시판 ID와 연결된 모든 파일 조회

    List<FileDTO> selectFilesBySeasonId(int seasonId); // 특정 시즌 ID와 연결된 모든 파일 조회

    void deleteFile(int fileId); // 특정 파일 삭제

    void deleteFilesBySeasonId(int seasonId); // 특정 시즌 ID와 연결된 모든 파일 데이터 삭제

    void deleteFilesByBoardId(int boardId);
}
