package com.globalmed.mes.mes_api.employee.shift.service;

import com.globalmed.mes.mes_api.employee.cert.service.EquipmentCertCheckService;
import com.globalmed.mes.mes_api.employee.repository.EmployeeRepo;
import com.globalmed.mes.mes_api.employee.shift.domain.ShiftAssignmentEntity;
import com.globalmed.mes.mes_api.employee.shift.domain.ShiftCalendarEntity;
import com.globalmed.mes.mes_api.employee.shift.dto.ShiftAssignmentDto;
import com.globalmed.mes.mes_api.employee.shift.dto.ShiftDto;
import com.globalmed.mes.mes_api.employee.shift.repository.ShiftAssignmentRepo;
import com.globalmed.mes.mes_api.employee.shift.repository.ShiftCalendarRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ShiftAssignmentService {
    private final ShiftAssignmentRepo assignmentRepo;
    private final ShiftCalendarRepo calendarRepo;
    private final EmployeeRepo employeeRepo;
    private final EquipmentCertCheckService equipmentCertCheckService;

    @Transactional
    public ShiftAssignmentEntity assignWorker(Long calendarId, String workerId) {
        // 1. 교대 달력 조회
        ShiftCalendarEntity calendar = calendarRepo.findById(calendarId)
                .orElseThrow(() -> new IllegalArgumentException("NOT_FOUND"));

        boolean employeeExists = employeeRepo.existsById(workerId);
        if (!employeeExists) {
            throw new IllegalArgumentException("WORKER_NOT_FOUND");
        }
        equipmentCertCheckService.check(workerId, calendar.getEquipmentId());

        // 2. 중복 배치 확인
        boolean exists = assignmentRepo.existsAssignment(
                calendar.getShiftDate(),
                calendar.getShift().getShiftId(),
                workerId,
                calendar.getEquipmentId(),
                calendar.getWorkcenterId()
        );
        if (exists) {
            throw new IllegalStateException("DUPLICATE_EMPLOYEE");
        }

        // 3. 배치 생성
        ShiftAssignmentEntity assignment = new ShiftAssignmentEntity();
        assignment.setShiftDate(calendar.getShiftDate());
        assignment.setShift(calendar.getShift());
        assignment.setWorkerId(workerId);
        assignment.setEquipmentId(calendar.getEquipmentId());
        assignment.setWorkcenterId(calendar.getWorkcenterId());
        assignment.setStartTs(calendar.getStartTs());
        assignment.setEndTs(calendar.getEndTs());

        return assignmentRepo.save(assignment);
    }

    public List<ShiftAssignmentDto> getAssignmentsByDate(LocalDate startDate, LocalDate endDate) {
        if (startDate == null) startDate = LocalDate.now();
        if (endDate == null) endDate = LocalDate.now();

        List<ShiftAssignmentEntity> assignments = assignmentRepo.findByShiftDateBetween(startDate, endDate);
        List<ShiftAssignmentDto> dtos = new ArrayList<>();

        for (ShiftAssignmentEntity a : assignments) {
            ShiftDto shiftDto = new ShiftDto(
                    a.getShift().getShiftId(),
                    a.getShift().getShiftCode(),
                    a.getShift().getShiftName(),
                    a.getShift().getStartTime().toString(),
                    a.getShift().getEndTime().toString()
            );
            dtos.add(new ShiftAssignmentDto(
                    a.getShiftDate(),
                    shiftDto,
                    a.getWorkerId(),
                    a.getEquipmentId(),
                    a.getWorkcenterId(),
                    a.getStartTs(),
                    a.getEndTs()
            ));
        }

        return dtos;
    }


}
