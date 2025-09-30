package com.globalmed.mes.mes_api.employee.repository;

import com.globalmed.mes.mes_api.employee.domain.EmployeeEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EmployeeRepo extends JpaRepository<EmployeeEntity, String> {

    @Query("SELECT e FROM EmployeeEntity e WHERE LOWER(e.employeeName) LIKE LOWER(CONCAT('%', :name, '%')) AND e.deleted = false")
    Page<EmployeeEntity> findEmployeeName(@Param("name") String name, Pageable pageable);

    @Query("SELECT e FROM EmployeeEntity e WHERE e.deleted = false")
    Page<EmployeeEntity> findNotDeletedAll(Pageable pageable);

    @Query("""
    SELECT DISTINCT eq.equipmentId
    FROM EmployeeEntity e
    JOIN EmployeeCertEntity ec ON ec.employee = e AND ec.deleted = false
    JOIN EquipmentCertEntity eqc ON eqc.cert = ec.cert AND eqc.deleted = false
    JOIN eqc.equipment eq
    WHERE e.employeeId = :employeeId
""")
    List<String> findAllowedEquipmentIds(@Param("employeeId") String employeeId);

    @Query("""
    SELECT DISTINCT p.processId
    FROM EmployeeEntity e
    JOIN EmployeeCertEntity ec ON ec.employee = e AND ec.deleted = false
    JOIN ProcessCertEntity pc ON pc.cert = ec.cert AND pc.deleted = false
    JOIN pc.process p
    WHERE e.employeeId = :employeeId
""")
    List<String> findAllowedProcessIds(@Param("employeeId") String employeeId);

//    @Query("SELECT e FROM EmployeeEntity e WHERE e.employeeId IN :employeeIds AND e.deleted = false")
//    List<EmployeeEntity> findShiftWorkers(@Param("employeeIds") List<String> employeeIds);

}
