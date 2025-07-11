package com.petlogue.duopetbackend.admin.model.dto;


import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class DashboardDataDto {
    private List<StatItemDto> genderStat;
}
