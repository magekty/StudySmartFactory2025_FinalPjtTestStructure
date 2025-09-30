package com.globalmed.mes.mes_api.employee.cert.service;

import com.globalmed.mes.mes_api.employee.cert.repository.ProcessCertRepo;
import com.globalmed.mes.mes_api.employee.repository.EmployeeCertRepo;
import com.globalmed.mes.mes_api.employee.shift.domain.ShiftAssignmentEntity;
import com.globalmed.mes.mes_api.employee.shift.repository.ShiftAssignmentRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProcessCertCheckService {
    private final ShiftAssignmentRepo shiftAssignmentRepo;
    private final EmployeeCertRepo employeeCertRepo;
    private final ProcessCertRepo processCertRepo;

    public void check(String equipmentId, String processId, OffsetDateTime now) {
        // 1) 현재 설비 담당자 찾기
        List<ShiftAssignmentEntity> assignments = shiftAssignmentRepo.findCurrentWorkers(equipmentId, now);
        if(assignments.isEmpty()) throw new IllegalStateException("NO_WORKER_ASSIGNED");

        for(var assignment : assignments){
            String employeeId = assignment.getWorkerId();
            Set<String> employeeCertCodes = employeeCertRepo.findByEmployee_EmployeeIdAndDeletedFalse(employeeId)
                    .stream()
                    .map(ec -> ec.getCert().getCertCode())
                    .collect(Collectors.toSet());

            Set<String> requiredCertCodes = processCertRepo.findProCert(processId)
                    .stream()
                    .map(pc -> pc.getCert().getCertCode())
                    .collect(Collectors.toSet());

            if (requiredCertCodes.isEmpty()) {
                return;
            }
            if(employeeCertCodes.containsAll(requiredCertCodes)){
                return; // 자격 있는 사람 한 명이라도 있으면 통과
            }
        }
        throw new IllegalStateException("WO_CERT_INVALID");
    }
}


