// rbac/MenuDto.java
package com.globalmed.mes.mes_api.rbac;


import java.util.List;

public record MenuDto(String code, String name, String path,
                      Perms perms, List<MenuDto> children) {
    public record Perms(boolean read, boolean write, boolean exec) {}
}

