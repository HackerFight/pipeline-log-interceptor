package com.qiuguan.common.log.trace.logbiz.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qiuguan.common.log.trace.bean.PipelineLogData;
import com.qiuguan.common.log.trace.bean.PipelineLogRequestParam;
import com.qiuguan.common.log.trace.enums.ModuleType;
import com.qiuguan.common.log.trace.logbiz.dao.ServicesLogMapper;
import com.qiuguan.common.log.trace.logbiz.entity.ServicesLog;
import com.qiuguan.common.log.trace.logbiz.service.ServicesLogService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 业务日志表(ServicesLog)表服务实现类
 *
 * @author makejava
 * @since 2023-08-02 11:01:07
 */
@Slf4j
@Service("servicesLogService")
public class ServicesLogServiceImpl extends ServiceImpl<ServicesLogMapper, ServicesLog> implements ServicesLogService {

    @Override
    public List<ServicesLog> findByTraceId(String traceId) {
        if (StringUtils.isBlank(traceId)) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<ServicesLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ServicesLog::getTraceId, traceId);

        return list(queryWrapper);
    }

    @Override
    public void removeLogsByTraceId(String traceId) {
        if (StringUtils.isBlank(traceId)) {
            return;
        }

        UpdateWrapper<ServicesLog> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("trace_id", traceId);

        remove(updateWrapper);
    }

    @Override
    public IPage<?> pipeline(Page<ServicesLog> page, PipelineLogRequestParam param) {
        LambdaQueryWrapper<ServicesLog> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(!Objects.isNull(param.getServicesScope()), ServicesLog::getServicesScope, param.getServicesScope());
        queryWrapper.like(StringUtils.isNotBlank(param.getPageName()), ServicesLog::getPageName, param.getPageName());
        queryWrapper.like(StringUtils.isNotBlank(param.getCreatorName()), ServicesLog::getCreatorName, param.getCreatorName());
        queryWrapper.like(StringUtils.isNotBlank(param.getLogIp()), ServicesLog::getLogIp, param.getLogIp());
        queryWrapper.like(StringUtils.isNotBlank(param.getLogRemark()), ServicesLog::getLogRemark, param.getLogRemark());
        queryWrapper.like(!Objects.isNull(param.getPageFunction()), ServicesLog::getPageFunction, param.getPageFunction());
        queryWrapper.like(StringUtils.isNotBlank(param.getTraceId()), ServicesLog::getTraceId, param.getTraceId());
        queryWrapper.eq(ServicesLog::getModuleType, ModuleType.CONTROLLER.getCode());

        if (!Objects.isNull(param.getStartTime())) {
            queryWrapper.gt(ServicesLog::getCreateTime, param.getStartTime());
        }

        if (!Objects.isNull(param.getEndTime())) {
            queryWrapper.lt(ServicesLog::getCreateTime, param.getEndTime());
        }
        queryWrapper.orderByDesc(ServicesLog::getCreateTime);

        IPage<ServicesLog> p = page(page, queryWrapper);
        return p.convert(x -> {
            PipelineLogData log = createLogBean(x);
            log.setLogRemark(parseJsonRemark(x.getLogRemark()));
            log.setLogUpdateRemark(parseJsonRemark(x.getLogUpdateRemark()));
            return log;
        });

    }

    private Object parseJsonRemark(String remark) {
        if (StringUtils.isBlank(remark)) {
            return null;
        }

        try {
            if (remark.startsWith("[")) {
                JSONArray objects = JSON.parseArray(remark);
                if (objects.size() == 1) {
                    return objects.get(0);
                }
                return objects;
            }

            return getObject(remark);

        } catch (Exception e) {
            log.warn("json 解析失败，忽略异常，原样输出， jsonStr: {}", remark); ;
        }

        return remark;
    }

    private static JSONObject getObject(String remark) {
        JSONObject result = new JSONObject();
        JSONObject jsonObject = JSON.parseObject(remark);
        for (String key : jsonObject.keySet()) {
            Object value = jsonObject.get(key);
            if (value instanceof String){
                //只解析一层，不必使用递归，如果解析失败，前端就原样展示
                String s = (String) value;
                if (s.startsWith("[")) {
                    JSONArray objects = JSON.parseArray(s);
                    result.put(key, objects);
                } else {
                    result.put(key, JSON.parseObject(s));
                }
            } else {
                result.put(key, JSON.toJSON(value));
            }
        }
        return result;
    }

    private PipelineLogData createLogBean(ServicesLog x) {
        PipelineLogData log = new PipelineLogData();
        log.setLogId(x.getLogId());
        log.setCreatorId(x.getCreatorId());
        log.setCreatorName(x.getCreatorName());
        log.setCreateTime(x.getCreateTime());
        log.setCause(x.getCause());
        log.setPageName(x.getPageName());
        log.setBusinessScope(x.getBusinessScope());
        log.setTraceId(x.getTraceId());
        log.setPageFunction(x.getPageFunction());
        log.setLogIp(x.getLogIp());
        log.setServicesScope(x.getServicesScope());
        return log;
    }
}

