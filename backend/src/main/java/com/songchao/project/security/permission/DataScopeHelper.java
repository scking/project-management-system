package com.songchao.project.security.permission;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.songchao.project.security.auth.AuthContext;
import com.songchao.project.security.auth.CurrentUser;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
public class DataScopeHelper {

    public <T> void applyDeptOrSelfScope(
            LambdaQueryWrapper<T> wrapper,
            Consumer<LambdaQueryWrapper<T>> deptScope,
            Consumer<LambdaQueryWrapper<T>> selfScope
    ) {
        CurrentUser currentUser = AuthContext.get();
        if (currentUser == null
                || currentUser.hasRole("SYSTEM_ADMIN")
                || currentUser.hasRole("super_admin")) {
            return;
        }
        Integer dataScope = currentUser.dataScope();
        if (dataScope == null || dataScope >= 5) {
            return;
        }
        if (dataScope >= 3) {
            deptScope.accept(wrapper);
            return;
        }
        selfScope.accept(wrapper);
    }
}
