package com.qiuguan.common.log.trace.interceptor;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.qiuguan.common.log.trace.enums.LogTypeEnum;
import com.qiuguan.common.log.trace.logbiz.entity.LogArgs;
import com.qiuguan.common.log.trace.logbiz.entity.ServicesLog;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author fu yuan hui
 * @date 2023-08-02 17:36:54 Wednesday
 */
@Component
public class InsertActionExecuteInterceptor extends AbstractActionExecuteInterceptor {

    @Override
    public boolean supports(LogTypeEnum logTypeEnum) {
        return logTypeEnum == LogTypeEnum.ADD;
    }

    @Override
    public void log(LogArgs logArgs) throws Exception {
        ServicesLog logBean = createLogBean(logArgs);
        String insetContent = resolveReadableSql(resolveSql(logArgs, logBean.getSqlParseMap()));
        logBean.setLogUpdateRemark(insetContent);
        logBean.setBusinessScope("Biz-ADD");
        logBean.setPageFunction(LogTypeEnum.ADD.getCode());

        saveLog(logBean);
    }


    /**
     * insert into category_instrument(instrument_name, instrument_type) values (磁性, 4);
     * @param sql
     * @return
     */
    @Override
    protected String resolveReadableSql(String sql) {
        return parseInsertStatement(sql.toLowerCase());
    }


    public static String parseInsertStatement(String insertStatement) {
        Map<String, String> map = new HashMap<>();

        // 匹配插入字段和对应值
        Pattern pattern = Pattern.compile("\\((.*)\\)\\s+values\\s+\\((.*)\\)");
        Matcher matcher = pattern.matcher(insertStatement);

        if (matcher.find()) {
            String fieldNames = matcher.group(1);
            String values = matcher.group(2);

            String[] fieldNameArr = fieldNames.split(",");
            String[] valueArr = values.split(",");

            for (int i = 0; i < fieldNameArr.length; i++) {
                String fieldName = fieldNameArr[i].trim().replaceAll("[`']", "");
                String valueStr = valueArr[i].trim().replaceAll("[`']", "");
                if (!StringUtils.isEmpty(fieldName)) {
                    map.put(fieldName, StringUtils.isEmpty(valueStr) ? "" : valueStr);
                }
            }
        }

        return CollectionUtils.isEmpty(map) ? "" : JSON.toJSONString(map);
    }
}
