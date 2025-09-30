package com.globalmed.mes.mes_api.employee.shift.repository;

import com.globalmed.mes.mes_api.employee.shift.domain.ShiftCalendarEntity;
import com.globalmed.mes.mes_api.employee.shift.domain.ShiftEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface ShiftCalendarRepo extends JpaRepository<ShiftCalendarEntity, Long> {
    boolean existsByShiftDateAndEquipmentIdAndShift(LocalDate shiftDate, String equipmentId, ShiftEntity shift);
}
