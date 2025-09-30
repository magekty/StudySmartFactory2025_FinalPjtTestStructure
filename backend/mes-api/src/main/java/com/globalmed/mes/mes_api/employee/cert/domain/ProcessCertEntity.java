package com.globalmed.mes.mes_api.employee.cert.domain;


import com.globalmed.mes.mes_api.process.domain.ProcessEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "tb_process_cert",
        uniqueConstraints = @UniqueConstraint(columnNames = {"process_id", "cert_id"}))
@Getter
@Setter
public class ProcessCertEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "process_cert_id")
    private Long processCertId; // PK

    // Process 참조
    @ManyToOne
    @JoinColumn(name = "process_id", nullable = false, foreignKey = @ForeignKey(name = "fk_proc_cert_proc"))
    private ProcessEntity process;

    // Cert 참조
    @ManyToOne
    @JoinColumn(name = "cert_id", nullable = false, foreignKey = @ForeignKey(name = "fk_proc_cert_cert"))
    private CertEntity cert;

    @Column(name="is_deleted", nullable = false)
    private boolean deleted = false;

    @Column(name="deleted_at")
    private OffsetDateTime deletedAt;
}
