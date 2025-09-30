package com.globalmed.mes.mes_api.employee.shift.controller;

import com.globalmed.mes.mes_api.employee.shift.domain.ShiftCalendarEntity;
import com.globalmed.mes.mes_api.employee.shift.service.ShiftCalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/shifts/calendars")
@RequiredArgsConstructor
public class ShiftCalendarController {
    private final ShiftCalendarService calendarService;

    // 날짜별 교대 달력 생성
    @PostMapping("/generate")
    @PreAuthorize("@permChecker.has(authentication, '/work-orders','write') or hasAnyRole('ADMIN','OP')")
    public ResponseEntity<List<ShiftCalendarEntity>> generateCalendar(
            @RequestParam String date,
            @RequestParam String equipmentId,
            @RequestParam String workcenterId
    ) {
        LocalDate targetDate = LocalDate.parse(date); // yyyy-MM-dd 형식
        List<ShiftCalendarEntity> calendars = calendarService.generateCalendarForDateAndEquipment(
                targetDate, equipmentId, workcenterId
        );
        return ResponseEntity.ok(calendars);
    }

}

