// rbac/MenuRow.java
package com.globalmed.mes.mes_api.rbac;

public interface MenuRow {
    String getMenuCode();
    String getMenuName();
    String getPath();
    Integer getAllowRead();
    Integer getAllowWrite();
    Integer getAllowExec();
}