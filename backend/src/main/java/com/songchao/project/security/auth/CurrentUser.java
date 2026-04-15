package com.songchao.project.security.auth;

import java.util.List;

public record CurrentUser(
        Long userId,
        String username,
        String realName,
        Long deptId,
        List<String> roleCodes,
        List<String> permissionCodes,
        Integer dataScope
) {
    public boolean hasPermission(String permission) {
        return permissionCodes != null && permissionCodes.contains(permission);
    }

    public boolean hasRole(String roleCode) {
        return roleCodes != null && roleCodes.contains(roleCode);
    }
}
