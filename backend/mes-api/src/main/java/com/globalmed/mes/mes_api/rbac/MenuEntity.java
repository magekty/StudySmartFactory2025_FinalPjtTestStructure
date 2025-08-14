// com.globalmed.mes.mes_api.rbac.MenuEntity
package com.globalmed.mes.mes_api.rbac;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity @Table(name = "tb_menu")
@Getter
@Setter
public class MenuEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "menu_id") private Long menuId;

    @Column(name = "menu_code", nullable = false, length = 100) private String menuCode;

    @Column(name = "menu_name", nullable = false, length = 150) private String menuName;

    @Column(name = "path", nullable = false, length = 255) private String path;

    @Column(name = "parent_id") private Long parentId;

    @Column(name = "sort_order") private Integer sortOrder;

    // parent_id 등 다른 컬럼은 필요 시 추가
}