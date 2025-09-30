// rbac/RoleEntity.java
package com.globalmed.mes.mes_api.rbac.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name="tb_role")
@Getter
@Setter
public class RoleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="role_id") private Long roleId;
    @Column(name="role_code", unique=true, nullable=false) private String roleCode;
    @Column(name="role_name", nullable=false) private String roleName;
}

