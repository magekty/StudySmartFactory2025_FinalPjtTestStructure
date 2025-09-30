package com.globalmed.mes.mes_api.employee.domain;

import com.globalmed.mes.mes_api.auth.domain.UserEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "tb_employee")
@Getter
@Setter
@NoArgsConstructor
public class EmployeeEntity {
    @Id
    @Column(name = "employee_id", length = 36)
    private String employeeId;  // tb_user.user_id를 그대로 사용

    @Column(name = "employee_number", nullable = false, length = 50, unique = true)
    private String employeeNumber;

    @Column(name = "employee_name", nullable = false, length = 100)
    private String employeeName;

    // 추가된 컬럼: 소속 부서
    @Column(name = "department_name", length = 100)
    private String departmentName;

    // 추가된 컬럼: 직원 상태. tb_code 테이블의 ID를 참조합니다.
    @Column(name = "status_code_id")
    private Long statusCodeId;

    @Column(name = "created_by", nullable = false, length = 50)
    private String createdBy;

    @Column(name = "modified_by", length = 50)
    private String modifiedBy;

    @Column(name="is_deleted", nullable = false)
    private boolean deleted = false;

    @Column(name="deleted_at")
    private OffsetDateTime deletedAt;

    // User와의 1:1 관계 (employee_id = user_id)
    @OneToOne
    @MapsId   // Employee의 PK(employee_id)가 User의 PK(user_id)와 공유됨
    @JoinColumn(name = "employee_id")
    private UserEntity user;

}
