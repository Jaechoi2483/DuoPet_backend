package com.petlogue.duopetbackend.admin.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatItemDto {
    private String item;
    private Long count;
}

