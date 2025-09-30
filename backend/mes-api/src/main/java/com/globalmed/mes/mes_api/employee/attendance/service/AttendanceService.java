package com.globalmed.mes.mes_api.employee.attendance.service;

import com.globalmed.mes.mes_api.employee.attendance.domain.AttendanceEntity;
import com.globalmed.mes.mes_api.employee.attendance.repository.AttendanceRepo;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AttendanceService {
    private final AttendanceRepo attendanceRepo;
    /**
     * 새로운 출퇴근 기록을 저장
     */
    @Transactional
    public AttendanceEntity saveAttendance(AttendanceEntity attendanceEntity) {
        return attendanceRepo.save(attendanceEntity);
    }
}
