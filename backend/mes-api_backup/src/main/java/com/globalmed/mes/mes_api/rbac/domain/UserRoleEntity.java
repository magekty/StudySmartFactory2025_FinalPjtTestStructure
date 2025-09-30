// rbac/UserRoleEntity.java
package com.globalmed.mes.mes_api.rbac.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name="tb_user_role",
        uniqueConstraints=@UniqueConstraint(name="uk_user_role", columnNames={"user_id","role_id"}))
@Getter @Setter
public class UserRoleEntity {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="user_role_id") private Long userRoleId;

    @Column(name="user_id", length=36, nullable=false) private String userId;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="role_id", nullable=false)
    private RoleEntity role;
}