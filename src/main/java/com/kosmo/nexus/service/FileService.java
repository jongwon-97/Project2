package com.kosmo.nexus.service;

import com.kosmo.nexus.dto.FileDTO;
import com.kosmo.nexus.dto.ImageDTO;

import java.util.List;

public interface FileService {
    void saveFile(FileDTO fileDTO);
    FileDTO getFileByBoardId(int boardId);
    void deleteFile(int fileId);
    List<FileDTO> getFilesByBoardId(int boardId);

//    List<ImageDTO> getImagesByBoardId(int boardId); // Board ID로 이미지 목록 조회
//    void deleteImagesByBoardId(int boardId); // Board ID로 이미지 데이터 삭제

    List<FileDTO> getFilesBySeasonId(int seasonId); // 시즌 ID로 파일 데이터 조회
    void deleteFilesBySeasonId(int seasonId);       // 시즌 ID로 파일 데이터 삭제

    void deleteFilesByBoardId(int boardId);

    void saveContent(ImageDTO imageDTO); // 이미지 및 텍스트 저장
    List<ImageDTO> getContentByBoardId(int boardId); // 게시글 ID로 이미지 및 텍스트 조회

    List<ImageDTO> getImagesByBoardId(int boardId); // 특정 board_id와 연결된 이미지 조회
    void deleteImagesByBoardId(int boardId);       // 특정 board_id와 연결된 이미지 삭제


    List<ImageDTO> getTextsByBoardId(int boardId);  // 특정 boardId로 텍스트 조회
    void deleteTextsByBoardId(int boardId);         // 특정 boardId로 텍스트 삭제
    void deleteOnlyImagesByBoardId(int boardId);


    void deleteTextById(int textId);

    void deleteImageById(int imageId);

    void updateContent(ImageDTO content);

    int getMaxContentOrder(int boardId, String contentType);
<<<<<<< HEAD

    String findContentTypeByFileId(Integer fileId);

    ImageDTO findImageDTObyFileId(int fileId);
    void deleteContentById(int fileId);
    int findContentOrder(int fileId);
    int getMaxContentOrderByBoardId(int boardId);
=======
>>>>>>> 874ad5901185699f331b024d1b121366d6f828bf
}
