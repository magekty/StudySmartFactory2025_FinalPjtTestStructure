package com.globalmed.mes.mes_api.employee.shift.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShiftDto {
    private Long shiftId;
    private String shiftCode;
    private String shiftName;
    private String startTime;
    private String endTime;
}