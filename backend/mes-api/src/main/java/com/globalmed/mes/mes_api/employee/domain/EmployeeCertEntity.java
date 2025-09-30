package com.globalmed.mes.mes_api.employee.domain;

import com.globalmed.mes.mes_api.employee.cert.domain.CertEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "tb_employee_cert",
        uniqueConstraints = @UniqueConstraint(columnNames = {"employee_id", "cert_id"}))
@Getter
@Setter
public class EmployeeCertEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // AUTO_INCREMENT
    @Column(name = "employee_cert_id")
    private Long employeeCertId; // PK

    // Employee 참조
    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false, foreignKey = @ForeignKey(name = "fk_employee_cert_emp"))
    private EmployeeEntity employee;

    // Cert 참조
    @ManyToOne
    @JoinColumn(name = "cert_id", nullable = false, foreignKey = @ForeignKey(name = "fk_employee_cert_cert"))
    private CertEntity cert;

    @Column(name = "employee_level_code_id", nullable = false)
    private Long employeeLevelCodeId; // 숙련도 코드 FK (tb_code)

    @Column(name = "cert_obtained_date", nullable = false)
    private LocalDate certObtainedDate; // 취득일

    @Column(name = "cert_expiry_date")
    private LocalDate certExpiryDate; // 만료일 (NULL=무기한)

    @Column(name = "created_by", nullable = false, length = 50)
    private String createdBy;

    @Column(name = "modified_by", length = 50)
    private String modifiedBy;

    @Column(name="is_deleted", nullable = false)
    private boolean deleted = false;

    @Column(name="deleted_at")
    private OffsetDateTime deletedAt;
}
