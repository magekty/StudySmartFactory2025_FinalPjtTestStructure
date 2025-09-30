package com.globalmed.mes.mes_api.employee.cert.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "tb_cert")
@Getter
@Setter
public class CertEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // AUTO_INCREMENT
    @Column(name = "cert_id")
    private Long certId;  // PK

    @Column(name = "cert_code", nullable = false, length = 50, unique = true)
    private String certCode;  // 자격 코드 (유일)

    @Column(name = "cert_name", nullable = false, length = 100)
    private String certName;  // 자격 명칭

    @Column(name = "cert_description", length = 255)
    private String certDescription;  // 설명

    @Column(name = "cert_valid_period_months")
    private Integer certValidPeriodMonths;  // 유효기간 (월 단위, NULL=무기한)

    @Column(name = "created_by", nullable = false, length = 50)
    private String createdBy;

    @Column(name = "modified_by", length = 50)
    private String modifiedBy;
}
