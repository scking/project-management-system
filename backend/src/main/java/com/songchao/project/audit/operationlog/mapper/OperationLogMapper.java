package com.songchao.project.audit.operationlog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.songchao.project.audit.operationlog.entity.OperationLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLog> {
}
