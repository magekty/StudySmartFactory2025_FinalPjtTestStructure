// rbac/MenuRepo.java
package com.globalmed.mes.mes_api.rbac;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

// com.globalmed.mes.mes_api.rbac.MenuRepo
public interface MenuRepo extends JpaRepository<MenuEntity, Long> {

    @Query(value = """
    select distinct
           m.menu_code   as menuCode,
           m.menu_name   as menuName,
           m.path        as path,
           rm.allow_read as allowRead,
           rm.allow_write as allowWrite,
           rm.allow_exec as allowExec
      from tb_user_role ur
      join tb_role_menu rm on rm.role_id = ur.role_id
      join tb_menu m      on m.menu_id   = rm.menu_id
     where ur.user_id = :uid
  """, nativeQuery = true)
    java.util.List<MenuRow> findMenusByUser(@Param("uid") String userId);
}