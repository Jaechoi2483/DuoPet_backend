package com.petlogue.duopetbackend.admin.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserReportCountDto {
    private Long userId;
    private String username;
    private int cumulativeReportCount;
}
