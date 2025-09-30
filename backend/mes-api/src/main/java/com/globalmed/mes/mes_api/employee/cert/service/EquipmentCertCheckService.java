package com.globalmed.mes.mes_api.employee.cert.service;

import com.globalmed.mes.mes_api.employee.cert.repository.EquipmentCertRepo;
import com.globalmed.mes.mes_api.employee.repository.EmployeeCertRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EquipmentCertCheckService {

    private final EmployeeCertRepo employeeCertRepo;
    private final EquipmentCertRepo equipmentCertRepo;

    /**
     * 직원이 설비를 사용할 자격 있는지 체크
     */
    public void check(String employeeId, String equipmentId) {
        // 설비가 요구하는 자격증
        var requiredCerts = equipmentCertRepo
                .findEquiprequier(equipmentId).stream()
                .map(ec -> ec.getCert().getCertCode())
                .toList();

        if (requiredCerts.isEmpty()) {
            return;
        }
        // 직원이 가진 자격증
        var employeeCerts = employeeCertRepo.findByEmployee_EmployeeIdAndDeletedFalse(employeeId).stream()
                .map(ec -> ec.getCert().getCertCode())
                .toList();

        boolean hasCert = employeeCerts.stream().anyMatch(requiredCerts::contains);
        if (!hasCert) {
            throw new IllegalStateException("WORKER_NO_CERT_FOR_EQUIPMENT");
        }
    }
}
