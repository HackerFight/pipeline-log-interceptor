package com.qiuguan.common.log.trace.chain;

import com.qiuguan.common.log.trace.ann.ChainOrder;
import com.qiuguan.common.log.trace.bean.LogTypePageNameBean;
import com.qiuguan.common.log.trace.bean.PermissionBean;
import com.qiuguan.common.log.trace.enums.IdentifyActionEnum;
import com.qiuguan.common.log.trace.enums.LogTypeEnum;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author fu yuan hui
 * @date 2023-08-23 09:50:23 Wednesday
 * @see ChainOrder 注解没有实际意义
 * <p>
 * 有一些接口(url) 一个请求对应多个业务功能逻辑，在做日志分析的时候无法识别出具体的动作，所以
 * 针对这些URL，单独处理一下，由于目前这类url不多，所以我暂时先使用Map进行缓存
 */
@ChainOrder(order = 0)
@Slf4j
public class MappingUrlMarkSearcher implements LogTypePageNameSearchChain {

    private final LogTypePageNameSearchChain logTypePageNameSearchChain;

    private final PermissionBean p;

    private final Object[] args;

    private final String url;

    private static final Map<String, String> URL_MARK_CACHE = Maps.newConcurrentMap();

    /* ----------------------------------xn start ----------------------------*/

    /**
     * @see com.efreight.xn.model.de.SaveOrderIdentifyDTO#pageType
     */
    private static final String ORDER_MARK_TYPE = "pageType";

    private static final String UN_MARK_TYPE = "unId";

    private static final String FACULTY_MARK_TYPE = "facultyId";


    /* ----------------------------------CSS start----------------------------*/
    private static final String CSS_TYPE = "type";

    private static final String CSS_RESTFUL_TYPE = "$type";
    private static final String CSS_TITLE_NAME= "titleName";

    private static final String CSS_DEBIT_NOTE_ID = "debitNoteId";

    private static final String CSS_BUSINESS_TYPE = "businessType";

    private static final String CSS_DEBIT_TYPE = "debitNote";

    private static final String CSS_WRITE_OFF_TITLE = "账单";

    private static final String CSS_BUSINESS_FLAG_INVOICE = "invoice";

    private static final String CSS_BUSINESS_FLAG_WRITE_OFF = "writeoff";

    private static final String CSS_BUSINESS_FLAG_DEBIT = "debit";

    //url: /cssIncomeInvoice/{type}/{id}/{rowUuid}/{b}
    private static final String CSS_CANCEL_BILL_APPLY_WITH_RESTFUL = "/cssIncomeInvoice/([^/]+)/([^/]+)/([^/]+)/([^/]+)";

    private static final Pattern PATTERN = Pattern.compile(CSS_CANCEL_BILL_APPLY_WITH_RESTFUL);


    @Override
    public LogTypePageNameBean retrieve() {
        final String url = p.getMappingUrl();
        if (URL_MARK_CACHE.containsKey(url)) {
            log.info("执行url特殊标记的匹配规则, 请求方法mapping url: {}, 实际请求url: {}", p.getMappingUrl(), this.url);

            final Object object = args[0];
            if (object != null) {
                String mark = URL_MARK_CACHE.get(url);

                switch (mark) {
                    case ORDER_MARK_TYPE:
                        return getOrderMark(object);
                    case UN_MARK_TYPE:
                        return getUnMark(object);
                    case FACULTY_MARK_TYPE:
                        return getFacultyMark(object);
                    case CSS_TYPE:
                        return getCssTypeMark(object);
                    case CSS_TITLE_NAME:
                        return getCssTitleMark(object);
                    case CSS_BUSINESS_TYPE:
                        return getCssBusinessTypeMark(object);
                    case CSS_DEBIT_NOTE_ID:
                        return getCssDebitNoteIdMark(object);
                    case CSS_RESTFUL_TYPE:
                        return getCssRestfulTypeMark(this.url);
                }
            }

        }

        if (logTypePageNameSearchChain != null) {
            return logTypePageNameSearchChain.retrieve();
        }

        return null;
    }

    private LogTypePageNameBean getCssRestfulTypeMark(String url) {
        Matcher matcher = PATTERN.matcher(url);
        if (matcher.find()) {
            //group(0)是完整url, group(1)是第一个参数，也就是type
            String type = matcher.group(1);
            if (CSS_DEBIT_NOTE_ID.equals(type)) {
                return new LogTypePageNameBean(LogTypeEnum.CANCEL_DEBIT_BILL_APPLY, LogTypeEnum.CANCEL_DEBIT_BILL_APPLY.getAction());
            }

            return new LogTypePageNameBean(LogTypeEnum.CANCEL_STATEMENT_BILL_APPLY, LogTypeEnum.CANCEL_STATEMENT_BILL_APPLY.getAction());

        }

        return new LogTypePageNameBean(LogTypeEnum.UNKNOWN, p.getPermissionName());
    }

    private static LogTypePageNameBean getCssDebitNoteIdMark(Object object) {
        Integer debitNoteId = (Integer) resolveProperty(object, CSS_DEBIT_NOTE_ID);
        if (null != debitNoteId) {
            return new LogTypePageNameBean(LogTypeEnum.DEBIT_BILL, LogTypeEnum.DEBIT_BILL.getAction());
        }

        return new LogTypePageNameBean(LogTypeEnum.STATEMENT_BILL, LogTypeEnum.STATEMENT_BILL.getAction());
    }

    private static LogTypePageNameBean getCssBusinessTypeMark(Object object) {
        String prop = (String) resolveProperty(object, CSS_BUSINESS_TYPE);
        if (CSS_BUSINESS_FLAG_INVOICE.equals(prop)) {
            return new LogTypePageNameBean(LogTypeEnum.UPLOAD, "上传发票附件");
        } else if (CSS_BUSINESS_FLAG_WRITE_OFF.equals(prop)) {
            return new LogTypePageNameBean(LogTypeEnum.UPLOAD, "上传核销单附件");
        } else if (CSS_BUSINESS_FLAG_DEBIT.equals(prop)) {
            return new LogTypePageNameBean(LogTypeEnum.UPLOAD, "上传账单附件");
        } else {
            //businessType = statement
            return new LogTypePageNameBean(LogTypeEnum.UPLOAD, "上传清单附件");
        }
    }

    private static LogTypePageNameBean getCssTitleMark(Object object) {
        String prop = (String) resolveProperty(object, CSS_TITLE_NAME);
        if (CSS_WRITE_OFF_TITLE.equals(prop)) {
            return new LogTypePageNameBean(LogTypeEnum.DEBIT_WRITE_OFF, LogTypeEnum.DEBIT_WRITE_OFF.getAction());
        }

        return new LogTypePageNameBean(LogTypeEnum.STATEMENT_WRITE_OFF, LogTypeEnum.STATEMENT_WRITE_OFF.getAction());
    }

    private static LogTypePageNameBean getCssTypeMark(Object object) {
        String prop = (String) resolveProperty(object, CSS_TYPE);
        if (CSS_DEBIT_TYPE.equals(prop)) {
            return new LogTypePageNameBean(LogTypeEnum.APPLY_DEBIT_BILL, LogTypeEnum.APPLY_DEBIT_BILL.getAction());
        }

        return new LogTypePageNameBean(LogTypeEnum.APPLY_STATEMENT_BILL, LogTypeEnum.APPLY_STATEMENT_BILL.getAction());
    }

    private LogTypePageNameBean getOrderMark(Object request) {
        String prop = (String)resolveProperty(request, ORDER_MARK_TYPE);
        IdentifyActionEnum identifyEnum = IdentifyActionEnum.fromCode(prop);
        return new LogTypePageNameBean(identifyEnum.getType(), getPageName(identifyEnum));
    }

    private String getPageName(IdentifyActionEnum identifyEnum) {
        String prefix = configPrefix(p.getPermission());
        if (StringUtils.isEmpty(prefix)) {
            return identifyEnum.getDescription();
        }
        return prefix + " - " + identifyEnum.getDescription();
    }

    private static LogTypePageNameBean getFacultyMark(Object request) {
        Object primaryKey = resolveProperty(request, FACULTY_MARK_TYPE);
        if (primaryKey == null) {
            return new LogTypePageNameBean(LogTypeEnum.ADD, "新增");
        }
        return new LogTypePageNameBean(LogTypeEnum.UPDATE, "修改");
    }

    private static LogTypePageNameBean getUnMark(Object request) {
        Integer primaryKey = (Integer) resolveProperty(request,UN_MARK_TYPE);
        Integer delFlag = (Integer) resolveProperty(request,"isDel");
        if (primaryKey == null) {
            return new LogTypePageNameBean(LogTypeEnum.ADD, "UN维护-新增");
        }
        Integer del = 1;
        if (del.equals(delFlag)) {
            return new LogTypePageNameBean(LogTypeEnum.DELETE, "UN维护-删除");
        }

        return new LogTypePageNameBean(LogTypeEnum.UPDATE, "UN维护-编辑");
    }

    static {
        URL_MARK_CACHE.put("/order/saveDaOrder", ORDER_MARK_TYPE);
        URL_MARK_CACHE.put("/order/saveChOrder", ORDER_MARK_TYPE);
        URL_MARK_CACHE.put("/order/saveMtOrder", ORDER_MARK_TYPE);
        URL_MARK_CACHE.put("/order/saveSdsOrder", ORDER_MARK_TYPE);

        URL_MARK_CACHE.put("/order/deOrder/identify", ORDER_MARK_TYPE);
        URL_MARK_CACHE.put("/order/saveChOrderIdentify", ORDER_MARK_TYPE);
        URL_MARK_CACHE.put("/order/saveDaOrderIdentify", ORDER_MARK_TYPE);
        URL_MARK_CACHE.put("/order/saveSdsOrderIdentify", ORDER_MARK_TYPE);
        URL_MARK_CACHE.put("/order/saveMtOrderIdentify", ORDER_MARK_TYPE);


        /**
         * @see com.efreight.category.entity.CategoryUn#unId
         */
        URL_MARK_CACHE.put("/un/saveUn", UN_MARK_TYPE);

        /**
         * @see com.efreight.tm.entity.XnFacultyManage#facultyId
         */
        URL_MARK_CACHE.put("/faculty/saveFaculty", FACULTY_MARK_TYPE);


        /* ------------------------------CSS--------------------------------- **/
        URL_MARK_CACHE.put("/cssIncomeInvoice/doSave", CSS_TYPE);
        URL_MARK_CACHE.put("/cssIncomeInvoiceDetailWriteoff/invoiceAuto", CSS_TITLE_NAME);
        URL_MARK_CACHE.put("/cssIncomeInvoice/{type}/{id}/{rowUuid}/{b}", CSS_RESTFUL_TYPE);
        URL_MARK_CACHE.put("/cssIncomeInvoiceDetailWriteoff/batchInvoiceAuto", CSS_TITLE_NAME);
        URL_MARK_CACHE.put("/cssIncomeInvoiceDetail/doSaveForDirectInvoicing", CSS_DEBIT_NOTE_ID);
        URL_MARK_CACHE.put("/cssIncomeFiles/saveOrModify", CSS_BUSINESS_TYPE);

    }

    public static Object resolveProperty(Object request, String fieldName) {
        try {
            Field field = ReflectionUtils.findField(request.getClass(), fieldName);
            if (field != null) {
                field.setAccessible(true);
                return field.get(request);
            }
        } catch (Exception e) {
            log.error("解析属性发生错误, obj: {}, field: {},", request, fieldName, e);
        }

        return null;
    }

    public MappingUrlMarkSearcher(LogTypePageNameSearchChain logTypePageNameSearchChain, PermissionBean p, Object[] args, String url) {
        this.logTypePageNameSearchChain = logTypePageNameSearchChain;
        this.p = p;
        this.args = args;
        this.url = url;
    }
}
