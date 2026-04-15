package com.songchao.project.audit.operationlog.service;

import com.songchao.project.audit.operationlog.entity.OperationLog;
import com.songchao.project.security.auth.AuthContext;
import com.songchao.project.security.auth.CurrentUser;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OperationLogQueryService {
    private final OperationLogService operationLogService;

    public OperationLogQueryService(OperationLogService operationLogService) {
        this.operationLogService = operationLogService;
    }

    public List<OperationLog> list(String moduleName, String operationType, String resultStatus) {
        List<OperationLog> logs = operationLogService.list();
        CurrentUser currentUser = AuthContext.get();
        return logs.stream()
                .filter(log -> moduleName == null || moduleName.isBlank() || contains(log.getModuleName(), moduleName))
                .filter(log -> operationType == null || operationType.isBlank() || operationType.equals(log.getOperationType()))
                .filter(log -> resultStatus == null || resultStatus.isBlank() || resultStatus.equals(log.getResultStatus()))
                .filter(log -> matchScope(log, currentUser))
                .collect(Collectors.toList());
    }

    private boolean contains(String source, String keyword) {
        return source != null && source.contains(keyword);
    }

    private boolean matchScope(OperationLog log, CurrentUser currentUser) {
        if (currentUser == null || currentUser.hasRole("SYSTEM_ADMIN")) {
            return true;
        }
        Integer dataScope = currentUser.dataScope();
        if (dataScope == null || dataScope >= 5) {
            return true;
        }
        if (dataScope >= 3 && currentUser.deptId() != null) {
            return currentUser.deptId().equals(log.getOperatorDeptId());
        }
        return currentUser.userId() != null && currentUser.userId().equals(log.getOperatorId());
    }
}
