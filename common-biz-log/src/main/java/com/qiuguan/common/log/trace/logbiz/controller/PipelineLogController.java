package com.qiuguan.common.log.trace.logbiz.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qiuguan.common.log.trace.logbiz.entity.ServicesLog;
import com.qiuguan.common.log.trace.logbiz.service.ServicesLogService;
import com.qiuguan.common.log.trace.bean.PipelineLogRequestParam;
import com.qiuguan.common.log.utils.MessageInfo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author fu yuan hui
 * @date 2023-08-08 18:28:26 Tuesday
 */
@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/pipeline/log")
public class PipelineLogController {

    private final ServicesLogService servicesLogService;

    @GetMapping("/list")
    public MessageInfo<?> pipelineLog(Page<ServicesLog> page, PipelineLogRequestParam param) {
        return MessageInfo.ok(this.servicesLogService.pipeline(page, param));
    }
}
