package com.globalmed.mes.mes_api.rbac;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuRepo menuRepo;

    public List<MenuDto> getMenusForUser(String userId) {
        return menuRepo.findMenusByUser(userId).stream()
                .map(r -> new MenuDto(
                        r.getMenuCode(),
                        r.getMenuName(),
                        r.getPath(),
                        new MenuDto.Perms(
                                r.getAllowRead() != null && r.getAllowRead() == 1,
                                r.getAllowWrite() != null && r.getAllowWrite() == 1,
                                r.getAllowExec() != null && r.getAllowExec() == 1
                        ),
                        List.of() // Parent/children tree structure is not built here.
                )).toList();
    }

    public List<MenuDto> getTreeForUser(String userId) {
        List<MenuFlatRow> rows = menuRepo.findMenusByUser(userId);

        // Filter out menus without read permission
        List<MenuFlatRow> readable = rows.stream()
                .filter(r -> r.getAllowRead() != null && r.getAllowRead() == 1)
                .toList();

        // Map children to their parent IDs
        Map<Long, List<MenuFlatRow>> byParent = readable.stream()
                .collect(Collectors.groupingBy(r -> Optional.ofNullable(r.getParentId()).orElse(0L)));

        // Define a comparator for sorting
        Comparator<MenuFlatRow> cmp = Comparator.comparing(
                (MenuFlatRow r) -> Optional.ofNullable(r.getSortOrder()).orElse(0)
        ).thenComparing(
                MenuFlatRow::getMenuName, Comparator.nullsLast(String::compareTo)
        );

        // Recursively build the tree from the root (parentKey = 0L)
        return buildChildren(0L, byParent, cmp, new HashSet<>(), 0);
    }

    private List<MenuDto> buildChildren(
            Long parentKey,
            Map<Long, List<MenuFlatRow>> byParent,
            Comparator<MenuFlatRow> cmp,
            Set<Long> visited,
            int depth
    ) {
        // Safety guard for deep recursion
        if (depth > 20) {
            return List.of();
        }

        List<MenuFlatRow> children = byParent.getOrDefault(parentKey, List.of());
        children = children.stream().sorted(cmp).toList();

        List<MenuDto> result = new ArrayList<>();

        for (MenuFlatRow r : children) {
            Long id = r.getMenuId();

            // Cycle guard
            if (id != null && !visited.add(id)) {
                continue;
            }

            List<MenuDto> sub = buildChildren(
                    Optional.ofNullable(id).orElse(-1L),
                    byParent,
                    cmp,
                    visited,
                    depth + 1
            );

            MenuDto.Perms perms = new MenuDto.Perms(
                    r.getAllowRead() != null && r.getAllowRead() == 1,
                    r.getAllowWrite() != null && r.getAllowWrite() == 1,
                    r.getAllowExec() != null && r.getAllowExec() == 1
            );

            result.add(new MenuDto(r.getMenuCode(), r.getMenuName(), r.getPath(), perms, sub));
        }
        return result;
    }
}