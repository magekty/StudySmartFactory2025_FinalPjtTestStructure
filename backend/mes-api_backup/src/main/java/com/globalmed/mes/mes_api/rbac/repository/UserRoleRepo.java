package com.globalmed.mes.mes_api.rbac.repository;

import com.globalmed.mes.mes_api.rbac.domain.UserRoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRoleRepo extends JpaRepository<UserRoleEntity, Long> {
    @Query("""
    select ur.role.roleCode
      from UserRoleEntity ur
     where ur.userId = :uid
  """)
    List<String> findRoleCodes(@Param("uid") String uid);
}