package com.qiuguan.common.log.trace.interceptor;

import com.alibaba.fastjson.JSON;
import com.qiuguan.common.log.trace.enums.LogTypeEnum;
import com.qiuguan.common.log.trace.logbiz.entity.LogArgs;
import com.qiuguan.common.log.trace.logbiz.entity.ServicesLog;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.ArrayUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author fu yuan hui
 * @date 2023-08-03 16:15:58 Thursday
 */
@SuppressWarnings("all")
@Slf4j
@Component
public class UpdateActionExecuteInterceptor extends AbstractActionExecuteInterceptor {

    @Override
    public boolean supports(LogTypeEnum logTypeEnum) {
        return LogTypeEnum.UPDATE == logTypeEnum;
    }


    @Override
    public void log(LogArgs logArgs) throws Exception {

        ServicesLog logBean = createLogBean(logArgs);
        //根据SQL获取更新的内容
        String updateContent = resolveReadableSql(resolveSql(logArgs, logBean.getSqlParseMap()));


        logBean.setPageFunction(LogTypeEnum.UPDATE.getCode());
        logBean.setBusinessScope("update");
        logBean.setLogUpdateRemark(updateContent);

        saveLog(logBean);
    }

    /**
     * forexample:
     * UPDATE category_instrument SET instrument_name=?, edit_time=?, editor_id=?, editor_name=? WHERE instrument_id=?
     */
    @Override
    protected String resolveReadableSql(String sql) {
        sql = sql.toLowerCase();
        sql = sql.replaceAll("'", "");
        sql = sql.substring(sql.indexOf("set") + 3, sql.indexOf("where"));
        String[] split = sql.trim().split(",");
        Map<String, String> map = new HashMap<>();
        for (String s : split) {
            String[] v = s.split("=");
            if (!ArrayUtils.isEmpty(v) && v.length == 2) {
                map.put(v[0], StringUtils.isEmpty(v[1]) ? "" : v[1]);
            }
        }

        return CollectionUtils.isEmpty(map) ? "" : JSON.toJSONString(map);
    }
}
