package com.globalmed.mes.mes_api.auth.repository;

import com.globalmed.mes.mes_api.auth.domain.UserEntity;
import org.springframework.data.jpa.repository.*;

import java.util.*;

// auth/UserRepo.java
public interface UserRepo extends JpaRepository<UserEntity, String> {
    Optional<UserEntity> findByUsername(String username);
}