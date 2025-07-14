package com.petlogue.duopetbackend.admin.model.dto;


import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class DashboardDataDto {
    private List<StatItemDto> genderStat;
    private List<StatItemDto> petCountStat;
    private List<StatItemDto> animalTypeStat;
    private List<StatItemDto> neuteredStat;
    private List<StatItemDto> summary;
}
