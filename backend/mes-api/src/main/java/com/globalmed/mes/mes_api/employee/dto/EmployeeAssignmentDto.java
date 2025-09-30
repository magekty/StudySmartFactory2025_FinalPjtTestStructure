package com.globalmed.mes.mes_api.employee.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeAssignmentDto {
    private String employeeId;
    private String employeeName;
    private String employeeNumber;
    private List<String> allowedEquipment; // 설비명 또는 ID
    private List<String> allowedProcess;   // 공정명 또는 ID
}
