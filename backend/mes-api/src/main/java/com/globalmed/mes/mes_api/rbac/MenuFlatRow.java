package com.globalmed.mes.mes_api.rbac;

public interface MenuFlatRow {
    Long getMenuId();
    Long getParentId();
    String getMenuCode();
    String getMenuName();
    String getPath();
    Integer getAllowRead();
    Integer getAllowWrite();
    Integer getAllowExec();
    Integer getSortOrder();
}