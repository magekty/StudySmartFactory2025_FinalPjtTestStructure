// rbac/MenuRepo.java
package com.globalmed.mes.mes_api.rbac.repository;

import com.globalmed.mes.mes_api.rbac.MenuFlatRow;
import com.globalmed.mes.mes_api.rbac.domain.MenuEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

// com.globalmed.mes.mes_api.rbac.repository.MenuRepo
public interface MenuRepo extends JpaRepository<MenuEntity, Long> {

//    @Query(value = """
//    select distinct
//           m.menu_code   as menuCode,
//           m.menu_name   as menuName,
//           m.path        as path,
//           rm.allow_read as allowRead,
//           rm.allow_write as allowWrite,
//           rm.allow_exec as allowExec
//      from tb_user_role ur
//      join tb_role_menu rm on rm.role_id = ur.role_id
//      join tb_menu m      on m.menu_id   = rm.menu_id
//     where ur.user_id = :uid
//  """, nativeQuery = true)
//    List<MenuRow> findMenusByUser(@Param("uid") String userId);

    @Query(value = """ 
           select m.menu_id as menuId, m.parent_id as parentId, 
           m.menu_code as menuCode, m.menu_name as menuName, m.path as path, 
           COALESCE(MAX(rm.allow_read), 0) as allowRead, COALESCE(MAX(rm.allow_write), 0) as allowWrite, 
           COALESCE(MAX(rm.allow_exec), 0) as allowExec, COALESCE(m.sort_order, 0) as sortOrder 
           from tb_user_role ur 
           join tb_role_menu rm on rm.role_id = ur.role_id 
           join tb_menu m on m.menu_id = rm.menu_id 
           where ur.user_id = :uid group by m.menu_id, m.parent_id, m.menu_code, m.menu_name, 
           m.path, m.sort_order order by m.parent_id, m.sort_order, m.menu_name 
           """, nativeQuery = true)
    List<MenuFlatRow> findMenusByUser(@Param("uid") String userId);

}
