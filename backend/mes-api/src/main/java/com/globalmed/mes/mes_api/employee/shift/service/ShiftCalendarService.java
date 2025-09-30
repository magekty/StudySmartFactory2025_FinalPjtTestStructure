package com.globalmed.mes.mes_api.employee.shift.service;


import com.globalmed.mes.mes_api.employee.shift.domain.ShiftCalendarEntity;
import com.globalmed.mes.mes_api.employee.shift.domain.ShiftEntity;
import com.globalmed.mes.mes_api.employee.shift.repository.ShiftCalendarRepo;
import com.globalmed.mes.mes_api.employee.shift.repository.ShiftRepo;
import com.globalmed.mes.mes_api.equipstatus.domain.EquipmentEntity;
import com.globalmed.mes.mes_api.equipstatus.repository.EquipmentRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ShiftCalendarService {
    private final ShiftRepo shiftRepo;
    private final ShiftCalendarRepo calendarRepo;
    private final EquipmentRepo equipmentRepo;
    @Transactional
    public List<ShiftCalendarEntity> generateCalendarForDateAndEquipment(LocalDate shiftDate, String equipmentId, String workcenterId) {
        // 1. 설비 존재 여부 확인t
        EquipmentEntity equipment = equipmentRepo.findById(equipmentId)
                .orElseThrow(() -> new IllegalArgumentException("NOT_FOUND"));
        // 2. 워크센터 매치 확인
        if (!equipment.getWorkcenter().getWorkcenterId().equals(workcenterId)) {
            throw new IllegalArgumentException("WC_MISMATCH");
        }
        List<ShiftEntity> shifts = shiftRepo.findAll();
        List<ShiftCalendarEntity> results = new ArrayList<>();

        for (ShiftEntity shift : shifts) {
            // 중복 확인
            if (calendarRepo.existsByShiftDateAndEquipmentIdAndShift(shiftDate, equipmentId, shift)) {
                throw new IllegalStateException("Date_E_WC_Overlaps");
            }
            // start/end LocalDateTime 생성
            LocalDateTime start = LocalDateTime.of(shiftDate, shift.getStartTime());
            LocalDateTime end = LocalDateTime.of(shiftDate, shift.getEndTime());

            // 만약 교대가 자정을 넘어가는 경우(예: 23:00 ~ 07:00)
            if (end.isBefore(start)) {
                end = end.plusDays(1);
            }

            ShiftCalendarEntity calendar = new ShiftCalendarEntity();

            OffsetDateTime startTz = start
                    .atZone(ZoneId.systemDefault())
                    .toOffsetDateTime()
                    .withOffsetSameInstant(ZoneOffset.UTC);

            OffsetDateTime endTz = end
                    .atZone(ZoneId.systemDefault())
                    .toOffsetDateTime()
                    .withOffsetSameInstant(ZoneOffset.UTC);


            calendar.setShiftDate(shiftDate);
            calendar.setShift(shift);
            calendar.setEquipmentId(equipmentId);
            calendar.setWorkcenterId(workcenterId);
            calendar.setStartTs(startTz); // KST 예시
            calendar.setEndTs(endTz);
            calendar.setCreatedBy("system");

            results.add(calendarRepo.save(calendar));
        }

        return results;
    }
}
