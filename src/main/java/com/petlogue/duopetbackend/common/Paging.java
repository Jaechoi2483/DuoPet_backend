package com.petlogue.duopetbackend.common;

import lombok.Data;

    @Data
    public class Paging {
        private int listCount;      // 전체 게시글 수
        private int limit;          // 한 페이지에 보여줄 게시글 수
        private int currentPage;    // 현재 페이지
        private int totalPage;      // 전체 페이지 수
        private int startPage;      // 페이지 그룹 시작 번호
        private int endPage;        // 페이지 그룹 끝 번호
        private int groupSize = 3;  // 페이지 그룹 크기 (예: 1~5, 6~10 등)
        private String urlMapping;  // 프론트 경로 (선택사항)

        public Paging(int listCount, int limit, int currentPage, String urlMapping) {
            this.listCount = listCount;
            this.limit = limit;
            this.currentPage = currentPage;
            this.urlMapping = urlMapping;
            calculate();
        }

        public void calculate() {
            this.totalPage = (int) Math.ceil((double) listCount / limit);

            int currentGroup = (int) Math.ceil((double) currentPage / groupSize);
            this.startPage = (currentGroup - 1) * groupSize + 1;
            this.endPage = Math.min(startPage + groupSize - 1, totalPage);
        }
    }
