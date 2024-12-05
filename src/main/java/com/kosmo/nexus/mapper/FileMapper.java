package com.kosmo.nexus.mapper;

import com.kosmo.nexus.dto.FileDTO;
import com.kosmo.nexus.dto.ImageDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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

    void insertContent(ImageDTO imageDTO);

    List<ImageDTO> selectContentByBoardId(@Param("boardId") int boardId);

    List<ImageDTO> getImagesByBoardId(int boardId); // 특정 board_id와 연결된 이미지 조회
    void deleteImagesByBoardId(int boardId);       // 특정 board_id와 연결된 이미지 삭제

    List<ImageDTO> getTextsByBoardId(int boardId);  // 특정 boardId로 텍스트 조회
    void deleteTextsByBoardId(int boardId);         // 특정 boardId로 텍스트 삭제


    void deleteOnlyImagesByBoardId(int boardId);

    void deleteTextById(Integer textId);

    void deleteImageById(Integer imageId);

    void deleteContentByIdAndType(int textId, String text);
    void deleteContentById(int fileId);

    void updateContent(ImageDTO content);

    int getMaxContentOrder(int boardId, String contentType);

    String findContentTypeByFileId(Integer fileId);

    ImageDTO findImageDTObyFileId(int fileId);

    int findContentOrder(int fileId);
    int getMaxContentOrderByBoardId(int boardId);

}
