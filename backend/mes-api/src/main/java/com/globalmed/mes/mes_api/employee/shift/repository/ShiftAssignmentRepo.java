package com.globalmed.mes.mes_api.employee.shift.repository;

import com.globalmed.mes.mes_api.employee.shift.domain.ShiftAssignmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface ShiftAssignmentRepo extends JpaRepository<ShiftAssignmentEntity, Long> {
    @Query("""
        SELECT sa
        FROM ShiftAssignmentEntity sa
        WHERE sa.equipmentId = :equipmentId
          AND :now BETWEEN sa.startTs AND sa.endTs
        """)
    List<ShiftAssignmentEntity> findCurrentWorkers(
            @Param("equipmentId") String equipmentId,
            @Param("now") OffsetDateTime now
    );
    @Query("""
        SELECT CASE WHEN COUNT(a) > 0 THEN TRUE ELSE FALSE END
        FROM ShiftAssignmentEntity a
        WHERE a.shiftDate = :shiftDate
          AND a.shift.shiftId = :shiftId
          AND a.workerId = :workerId
          AND a.equipmentId = :equipmentId
          AND a.workcenterId = :workcenterId
    """)
    boolean existsAssignment(
            @Param("shiftDate") LocalDate shiftDate,
            @Param("shiftId") Long shiftId,
            @Param("workerId") String workerId,
            @Param("equipmentId") String equipmentId,
            @Param("workcenterId") String workcenterId
    );

    List<ShiftAssignmentEntity> findByShiftDateBetween(LocalDate startDate, LocalDate endDate);

    @Query("""
        SELECT sa 
        FROM ShiftAssignmentEntity sa
        WHERE sa.equipmentId = :equipmentId
          AND :now BETWEEN sa.startTs AND sa.endTs 
    """)
    Optional<ShiftAssignmentEntity> findEquipShiftNow(@Param("equipmentId") String equipmentId,
                                                      @Param("now") OffsetDateTime now);

}
