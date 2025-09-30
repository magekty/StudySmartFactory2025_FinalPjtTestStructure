package com.globalmed.mes.mes_api.auth.domain;

// auth/UserEntity.java

import jakarta.persistence.*;
import lombok.Getter; import lombok.Setter;

@Entity @Table(name="TB_USER")
@Getter @Setter
public class UserEntity {
    @Id @Column(name="user_id", length=36) private String userId;
    @Column(name="username", unique=true) private String username;
    @Column(name="password_hash") private String passwordHash;
    @Column(name="password_algo") private String passwordAlgo;
    @Column(name="is_active") private byte isActive;
}