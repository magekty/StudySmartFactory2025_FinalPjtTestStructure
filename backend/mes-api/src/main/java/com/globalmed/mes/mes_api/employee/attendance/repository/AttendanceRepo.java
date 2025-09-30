package com.globalmed.mes.mes_api.employee.attendance.repository;

import com.globalmed.mes.mes_api.employee.attendance.domain.AttendanceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttendanceRepo extends JpaRepository<AttendanceEntity, Long> {

}
