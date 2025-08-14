package com.globalmed.mes.mes_api.code;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name="TB_CODE",
        uniqueConstraints = @UniqueConstraint(name="uk_code_group_code", columnNames={"group_code","code"}))
@Getter
@Setter
public class CodeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="code_id") private Long codeId;
    @Column(name="group_code", nullable=false, length=50) private String groupCode;
    @Column(name="code", nullable=false, length=50) private String code;
    @Column(name="name", nullable=false, length=100) private String name;
    @Column(name="use_yn", nullable=false, length=1) private char useYn; // 'Y'/'N'
}