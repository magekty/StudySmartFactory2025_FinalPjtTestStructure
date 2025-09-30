package com.globalmed.mes.mes_api.employee.shift.controller;

import com.globalmed.mes.mes_api.employee.shift.domain.ShiftAssignmentEntity;
import com.globalmed.mes.mes_api.employee.shift.dto.ShiftAssignmentDto;
import com.globalmed.mes.mes_api.employee.shift.service.ShiftAssignmentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/shifts")
@RequiredArgsConstructor
public class ShiftAssignmentController {
    private final ShiftAssignmentService assignmentService;

    @PostMapping("/{calendarId}/assign")
    @PreAuthorize("@permChecker.has(authentication, '/work-orders','write') or hasAnyRole('ADMIN','OP')")
    public ResponseEntity<ShiftAssignmentEntity> assignWorker(
            @PathVariable Long calendarId,
            @RequestParam String workerId
    ) {
        ShiftAssignmentEntity assignment = assignmentService.assignWorker(calendarId, workerId);
        return ResponseEntity.ok(assignment);
    }
    @GetMapping("/assignments")
    public ResponseEntity<?> getAssignmentsByPeriod(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            HttpServletRequest req
    ) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            return ResponseEntity.badRequest().body(Map.of(
                    "code", "INVALID_DATE_RANGE",
                    "message", "startDate는 endDate 이전이어야 합니다.",
                    "path", req.getRequestURI(),
                    "method", req.getMethod()
            ));
        }

        List<ShiftAssignmentDto> assignments = assignmentService.getAssignmentsByDate(startDate, endDate);

        if (assignments == null || assignments.isEmpty()) {
            // 데이터가 없으면 404와 에러 메시지 반환
            return ResponseEntity.status(404).body(Map.of(
                    "code", "DATA_NOT_FOUND",
                    "message", "해당 기간의 근무 배정 데이터가 존재하지 않습니다",
                    "path", req.getRequestURI(),
                    "method", req.getMethod()
            ));
        }
        return ResponseEntity.ok(assignments);
    }
}