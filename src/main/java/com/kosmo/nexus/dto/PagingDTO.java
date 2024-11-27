package com.kosmo.nexus.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@ToString
public class PagingDTO {
    //페이징 처리 관련 프로퍼티
    private int pageNum = 1; //현재 보여줄 페이지 번호
    private int oneRecordPage; //한 페이지 당 보여줄 목록 개수
    private int totalCount; //총 게시글 수. DB에서 가져와 설정할 예정
    private int pageCount; //총 게시글 수와 oneRecordPage와의 연산으로 설정
    //DB에서 레코드를 끊어오기 위한 프로퍼티
    private int start;
    private int end;
    //페이징 블럭처리를 위한 프로퍼티
    private int prevBlock; //이전 5개 (이전블럭)
    private int nextBlock; //이후 5개 (다음블럭)
    private int pagingBlock = 5; //한 블럭 당 보여줄 페이지 수
    //검색관련 프로퍼티
    private String findType; //검색유형
    private String findKeyword; //검색어

    public void init() {
        // 총 페이지 수 계산
        pageCount = (totalCount - 1) / oneRecordPage + 1;

        // 현재 페이지 번호 유효성 검증
        if (pageNum < 1) {
            pageNum = 1; // 최소 1페이지
        }
        if (pageNum > pageCount) {
            pageNum = pageCount; // 최대 페이지로 제한
        }

        // LIMIT의 시작 값 계산
        start = (pageNum - 1) * oneRecordPage;

        // 페이징 블록 계산
        prevBlock = ((pageNum - 1) / pagingBlock) * pagingBlock;
        nextBlock = prevBlock + (pagingBlock + 1);

        // 유효한 블럭 값 보정
        if (nextBlock > pageCount) {
            nextBlock = pageCount + 1; // 다음 블럭이 총 페이지 수를 초과하지 않도록 설정
        }
        if (prevBlock < 0) {
            prevBlock = 0; // 이전 블럭이 음수가 되지 않도록 설정
        }
    }

    // 새 메서드: 총 게시글 수 설정과 초기화를 동시에 처리
    public void setTotalCountAndInit(int totalCount) {
        this.totalCount = totalCount; // 총 게시글 수 설정
        this.init(); // 초기화 로직 호출
    }
}
