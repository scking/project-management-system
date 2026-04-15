package com.songchao.project.security.permission;

import com.songchao.project.common.exception.BizException;
import com.songchao.project.security.auth.AuthContext;
import com.songchao.project.security.auth.CurrentUser;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
public class PermissionAspect {

    @Before("@within(org.springframework.web.bind.annotation.RestController) && execution(* *(..))")
    public void checkPermission(JoinPoint joinPoint) {
        Method method = resolveMethod(joinPoint);
        RequirePermission permission = AnnotationUtils.findAnnotation(method, RequirePermission.class);
        if (permission == null) {
            permission = AnnotationUtils.findAnnotation(joinPoint.getTarget().getClass(), RequirePermission.class);
        }
        if (permission == null) {
            return;
        }
        CurrentUser currentUser = AuthContext.get();
        if (currentUser == null) {
            throw new BizException("NOT_AUTHENTICATED");
        }
        if (currentUser.hasRole("SYSTEM_ADMIN") || currentUser.hasRole("super_admin") || currentUser.hasPermission(permission.value())) {
            return;
        }
        throw new BizException("NO_PERMISSION:" + permission.value());
    }

    private Method resolveMethod(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        Class<?>[] parameterTypes = new Class<?>[joinPoint.getArgs().length];
        for (int i = 0; i < joinPoint.getArgs().length; i++) {
            parameterTypes[i] = joinPoint.getArgs()[i] == null ? Object.class : joinPoint.getArgs()[i].getClass();
        }
        for (Method method : joinPoint.getTarget().getClass().getMethods()) {
            if (method.getName().equals(methodName) && method.getParameterCount() == joinPoint.getArgs().length) {
                return method;
            }
        }
        throw new IllegalStateException("无法解析方法: " + methodName);
    }
}
