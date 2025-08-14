package com.globalmed.mes.mes_api.auth;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.*;

// auth/UserRepo.java
public interface UserRepo extends JpaRepository<UserEntity, String> {
    Optional<UserEntity> findByUsername(String username);
}