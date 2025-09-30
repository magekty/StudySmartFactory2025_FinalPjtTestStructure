package com.globalmed.mes.mes_api.employee.attendance.controller;

import com.globalmed.mes.mes_api.code.CodeRepo;
import com.globalmed.mes.mes_api.employee.attendance.domain.AttendanceEntity;
import com.globalmed.mes.mes_api.employee.attendance.dto.AttendanceDto;
import com.globalmed.mes.mes_api.employee.attendance.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;


@RestController
@RequestMapping("/user/attendance")
@RequiredArgsConstructor
public class AttendanceController {
    private final AttendanceService attendanceService;
    private final CodeRepo codeRepo; // CodeRepo를 주입하여 코드 조회

    @PostMapping
    public ResponseEntity<AttendanceEntity> createAttendance(@RequestBody AttendanceDto.AttendanceRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).body(null); // Unauthorized
        }

        String userId = (String) auth.getPrincipal();
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        // 1. 요청받은 status로 CodeEntity를 조회합니다.
        // "ATTENDANCE_STATUS"는 출근/퇴근 상태를 구분하는 코드 그룹입니다.
        // 실제 코드 그룹명은 프로젝트에 맞게 수정해야 합니다.
        //
        return codeRepo.findByGroupCodeAndCode("ATTENDANCE_STATUS", request.getStatus())
                .map(statusCode -> {
                    // 2. 새로운 AttendanceEntity를 생성하고 자동으로 값을 채웁니다.
                    AttendanceEntity attendanceEntity = new AttendanceEntity();
                    attendanceEntity.setUserId(userId);
                    attendanceEntity.setCheckTime(now);
                    attendanceEntity.setStatusCode(statusCode);

                    // 3. 완전히 준비된 Entity를 서비스로 전달해 저장합니다.
                    AttendanceEntity savedAttendance = attendanceService.saveAttendance(attendanceEntity);
                    return ResponseEntity.ok(savedAttendance);
                })
                .orElse(ResponseEntity.badRequest().body(null)); // 유효하지 않은 status 코드일 경우 400 Bad Request 반환
    }
}
