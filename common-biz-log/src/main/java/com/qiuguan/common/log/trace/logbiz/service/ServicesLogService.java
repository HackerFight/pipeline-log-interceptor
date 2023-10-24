package com.qiuguan.common.log.trace.logbiz.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.qiuguan.common.log.trace.bean.PipelineLogRequestParam;
import com.qiuguan.common.log.trace.logbiz.entity.ServicesLog;

import java.util.List;

/**
 * 业务日志表(ServicesLog)表服务接口
 *
 * @author makejava
 * @since 2023-08-02 11:01:04
 */
public interface ServicesLogService extends IService<ServicesLog> {

    List<ServicesLog> findByTraceId(String traceId);

    void removeLogsByTraceId(String s);

    IPage<?> pipeline(Page<ServicesLog> page, PipelineLogRequestParam param);

}

