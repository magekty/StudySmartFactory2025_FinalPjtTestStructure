package com.globalmed.mes.mes_api.employee.repository;

import com.globalmed.mes.mes_api.employee.domain.EmployeeCertEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EmployeeCertRepo extends JpaRepository<EmployeeCertEntity, Long> {
    List<EmployeeCertEntity> findByEmployee_EmployeeIdAndDeletedFalse(String employeeId);
    @Query("SELECT ec FROM EmployeeCertEntity ec " +
            "JOIN FETCH ec.cert c " +
            "WHERE ec.employee.employeeId IN :employeeIds " +
            "AND ec.deleted = false")
    List<EmployeeCertEntity> findEmployeeCerts(@Param("employeeIds") List<String> employeeIds);

}
