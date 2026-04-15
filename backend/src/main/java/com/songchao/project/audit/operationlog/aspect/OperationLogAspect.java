package com.songchao.project.audit.operationlog.aspect;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.songchao.project.audit.operationlog.annotation.OperationLogRecord;
import com.songchao.project.audit.operationlog.entity.OperationLog;
import com.songchao.project.audit.operationlog.service.OperationLogService;
import com.songchao.project.security.auth.AuthContext;
import com.songchao.project.security.auth.CurrentUser;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.UUID;

@Aspect
@Component
public class OperationLogAspect {
    private final OperationLogService operationLogService;
    private final ObjectMapper objectMapper;

    public OperationLogAspect(OperationLogService operationLogService, ObjectMapper objectMapper) {
        this.operationLogService = operationLogService;
        this.objectMapper = objectMapper;
    }

    @Around("@annotation(record)")
    public Object around(ProceedingJoinPoint joinPoint, OperationLogRecord record) throws Throwable {
        OperationLog log = buildBaseLog(record, joinPoint.getArgs());
        try {
            Object result = joinPoint.proceed();
            log.setResultStatus("SUCCESS");
            log.setAfterData(toJson(result));
            log.setBizId(extractBizId(result));
            operationLogService.save(log);
            return result;
        } catch (Throwable ex) {
            log.setResultStatus("FAILED");
            log.setErrorMessage(truncate(ex.getMessage(), 500));
            operationLogService.save(log);
            throw ex;
        }
    }

    private OperationLog buildBaseLog(OperationLogRecord record, Object[] args) {
        OperationLog log = new OperationLog();
        log.setTraceId(UUID.randomUUID().toString().replace("-", ""));
        log.setModuleName(record.module());
        log.setBizType(record.bizType());
        log.setOperationType(record.operation());
        log.setOperationDesc(record.description());
        log.setBeforeData(toJson(args == null ? new Object[0] : Arrays.stream(args).toArray()));
        log.setCreatedAt(LocalDateTime.now().toString());
        CurrentUser currentUser = AuthContext.get();
        if (currentUser != null) {
            log.setOperatorId(currentUser.userId());
            log.setOperatorDeptId(currentUser.deptId());
            log.setOperatorName(currentUser.realName());
        }
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            log.setRequestMethod(request.getMethod());
            log.setRequestUri(request.getRequestURI());
            log.setOperatorIp(request.getRemoteAddr());
        }
        return log;
    }

    private Long extractBizId(Object result) {
        if (result == null) {
            return null;
        }
        try {
            Object data = result.getClass().getMethod("data").invoke(result);
            if (data == null) {
                return null;
            }
            return (Long) data.getClass().getMethod("getId").invoke(data);
        } catch (Exception ignored) {
            return null;
        }
    }

    private String toJson(Object value) {
        try {
            return truncate(objectMapper.writeValueAsString(value), 2000);
        } catch (JsonProcessingException e) {
            return truncate(String.valueOf(value), 2000);
        }
    }

    private String truncate(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }
}
