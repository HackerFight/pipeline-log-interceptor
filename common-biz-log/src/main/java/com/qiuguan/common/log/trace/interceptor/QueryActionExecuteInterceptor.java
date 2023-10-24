package com.qiuguan.common.log.trace.interceptor;


import com.qiuguan.common.log.trace.enums.LogTypeEnum;
import com.qiuguan.common.log.trace.logbiz.entity.LogArgs;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author fu yuan hui
 * @date 2023-08-02 18:03:02 Wednesday
 */
@Component
public class QueryActionExecuteInterceptor extends AbstractActionExecuteInterceptor {

    @Override
    public boolean supports(LogTypeEnum logTypeEnum) {
        return logTypeEnum == LogTypeEnum.QUERY;
    }

    @Override
    public void log(LogArgs logArgs) throws Exception {
        //nothing to do...
    }

    /**
     * select * from where id = and age = 3 and salary > 5 and salary < 10;
     */
    @Override
    protected String resolveReadableSql(String sql) {
        List<String> sqlConditionList = new ArrayList<>();
        sql = sql.toLowerCase();
        //有可能全表查，不带条件
        if (sql.contains("where")) {
            sql = sql.substring(sql.indexOf("where") + 5);
            String[] split = sql.split("and");
            sqlConditionList.addAll(Arrays.asList(split));
        }

        return CollectionUtils.isEmpty(sqlConditionList) ? "" : sqlConditionList.toString();
    }

}
