package com.qiuguan.common.log.trace.logbiz.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qiuguan.common.log.trace.logbiz.entity.ServicesLog;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;

/**
 * 业务日志表(ServicesLog)表数据库访问层
 *
 * @author makejava
 * @since 2023-08-02 11:00:59
 */
public interface ServicesLogMapper extends BaseMapper<ServicesLog> {

    @Delete("delete from services_log where trace_id = #{traceId}")
    void removeLogsByTraceId(@Param("traceId") String traceId);;
}

