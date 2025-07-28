package com.petlogue.duopetbackend.admin.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShoppingResponseDto {
    private Long total; // 전체 검색 결과 개수
    private List<Item> items;
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Item {
        private String title;
        private String link;
        private String image;
        private Integer lprice; // 최저가
    }
}
