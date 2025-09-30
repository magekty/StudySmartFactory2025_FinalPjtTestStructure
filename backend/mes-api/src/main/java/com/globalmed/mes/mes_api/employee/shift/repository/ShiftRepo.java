package com.globalmed.mes.mes_api.employee.shift.repository;

import com.globalmed.mes.mes_api.employee.shift.domain.ShiftEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShiftRepo extends JpaRepository<ShiftEntity, Long> {
}
