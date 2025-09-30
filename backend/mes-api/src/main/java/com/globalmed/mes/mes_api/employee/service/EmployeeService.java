package com.globalmed.mes.mes_api.employee.service;

import com.globalmed.mes.mes_api.employee.cert.repository.EquipmentCertRepo;
import com.globalmed.mes.mes_api.employee.cert.repository.ProcessCertRepo;
import com.globalmed.mes.mes_api.employee.domain.EmployeeEntity;
import com.globalmed.mes.mes_api.employee.dto.EmployeeAssignmentDto;
import com.globalmed.mes.mes_api.employee.repository.EmployeeRepo;
import com.globalmed.mes.mes_api.equipstatus.repository.EquipmentRepo;
import com.globalmed.mes.mes_api.process.repository.ProcessRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmployeeService {
    private final EmployeeRepo employeeRepo;
    private final EquipmentRepo equipmentRepo; // 필요
    private final ProcessRepo processRepo;     // 필요
    private final EquipmentCertRepo equipmentCertRepo; // 설비-자격증 매핑 Repo
    private final ProcessCertRepo processCertRepo;     // 공정-자격증 매핑 Repo

    public Page<EmployeeAssignmentDto> getEmployeesWithAssignments(String name, Pageable pageable) {
        // 1. 기본 직원 페이징 조회 (기존과 동일)
        Page<EmployeeEntity> employees;
        if (name != null && !name.isEmpty()) {
            employees = employeeRepo.findEmployeeName(name, pageable);
        } else {
            employees = employeeRepo.findNotDeletedAll(pageable);
        }

        // 2. 모든 설비와 공정 ID 목록을 미리 조회
        List<String> allEquipmentIds = equipmentRepo.findAllEquipmentIds(); // 예: SELECT equipmentId FROM EquipmentEntity
        List<String> allProcessIds = processRepo.findAllProcessIds();     // 예: SELECT processId FROM ProcessEntity

        // 3. 직원별 DTO 변환 및 '가능' 여부 판단
        List<EmployeeAssignmentDto> dtos = new ArrayList<>();
        for (EmployeeEntity employee : employees.getContent()) {

            // 3-1. 해당 직원이 가진 자격증으로 '가능한' 설비/공정 ID 목록 조회
            List<String> allowedByCertEquipmentIds = employeeRepo.findAllowedEquipmentIds(employee.getEmployeeId());
            List<String> allowedByCertProcessIds = employeeRepo.findAllowedProcessIds(employee.getEmployeeId());

            // 3-2. 최종 목록 생성 (자격증 필요 유무 판단 로직)
            List<String> finalAllowedEquipment = new ArrayList<>();
            List<String> finalAllowedProcess = new ArrayList<>();

            // 3-3. 설비 목록 순회
            for (String eqId : allEquipmentIds) {
                // 설비에 필수 자격증이 있는지 확인 (EquipmentCertRepo 필요)
                boolean isCertRequired = equipmentCertRepo.existsByEquipment_EquipmentId(eqId);

                if (!isCertRequired) {
                    // 자격증 필요 없음: 누구나 가능
                    finalAllowedEquipment.add(eqId);
                } else if (allowedByCertEquipmentIds.contains(eqId)) {
                    // 자격증 필요함: 직원이 자격증을 보유하고 있어 가능
                    finalAllowedEquipment.add(eqId);
                }
            }

            // 3-4. 공정 목록 순회 (설비와 동일 로직)
            for (String procId : allProcessIds) {
                // 공정에 필수 자격증이 있는지 확인 (ProcessCertRepo 필요)
                boolean isCertRequired = processCertRepo.existsByProcess_ProcessId(procId);

                if (!isCertRequired) {
                    // 자격증 필요 없음: 누구나 가능
                    finalAllowedProcess.add(procId);
                } else if (allowedByCertProcessIds.contains(procId)) {
                    // 자격증 필요함: 직원이 자격증을 보유하고 있어 가능
                    finalAllowedProcess.add(procId);
                }
            }


            // 3-5. DTO 생성 및 추가
            EmployeeAssignmentDto dto = new EmployeeAssignmentDto(
                    employee.getEmployeeId(),
                    employee.getEmployeeName(),
                    employee.getEmployeeNumber(),
                    finalAllowedEquipment,
                    finalAllowedProcess
            );
            dtos.add(dto);
        }

        // 4. PageImpl로 반환 (기존과 동일)
        return new PageImpl<>(dtos, pageable, employees.getTotalElements());
    }

}
