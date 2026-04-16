package com.songchao.project.modules.auth.vo;

import java.util.List;

public record AuthUserVO(
        Long userId,
        String username,
        String realName,
        Long deptId,
        String deptName,
        List<String> roleCodes,
        List<String> permissionCodes,
        Integer dataScope,
        String accessToken
) {
}
