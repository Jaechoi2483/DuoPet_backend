package com.petlogue.duopetbackend.admin.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StatItemDto {
    private String label;
    private long value;
}
