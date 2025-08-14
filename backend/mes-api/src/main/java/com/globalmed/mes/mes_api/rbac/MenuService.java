// rbac/MenuService.java
package com.globalmed.mes.mes_api.rbac;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Service @RequiredArgsConstructor
public class MenuService {
    private final MenuRepo menuRepo;
    public List<MenuDto> getMenusForUser(String userId){
        return menuRepo.findMenusByUser(userId).stream()
                .map(r -> new MenuDto(
                        r.getMenuCode(), r.getMenuName(), r.getPath(),
                        new MenuDto.Perms(r.getAllowRead()!=null && r.getAllowRead()==1,
                                r.getAllowWrite()!=null && r.getAllowWrite()==1,
                                r.getAllowExec()!=null && r.getAllowExec()==1),
                        List.of() // parent/children 트리 구성은 후속(지금은 플랫)
                )).toList();
    }
}
