package com.globalmed.mes.mes_api.employee.attendance.dto;

import lombok.*;

public class AttendanceDto {
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AttendanceRequest{
        private String status;
    }
}
