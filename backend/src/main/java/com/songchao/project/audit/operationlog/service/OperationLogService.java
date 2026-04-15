package com.songchao.project.audit.operationlog.service;

import com.songchao.project.audit.operationlog.entity.OperationLog;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class OperationLogService {
    private final List<OperationLog> logs = new ArrayList<>();
    private final AtomicLong idGenerator = new AtomicLong(0);

    public OperationLogService() {}

    public synchronized void save(OperationLog entity) {
        entity.setId(idGenerator.incrementAndGet());
        logs.add(0, entity);
    }

    public synchronized List<OperationLog> list() {
        return new ArrayList<>(logs);
    }
}
