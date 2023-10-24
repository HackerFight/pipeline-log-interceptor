package com.qiuguan.common.log.trace.bean;

import com.qiuguan.common.log.trace.enums.LogTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author fu yuan hui
 * @date 2023-08-23 09:55:04 Wednesday
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class LogTypePageNameBean implements Serializable {

    private LogTypeEnum logTypeEnum;

    private String pageName;
}
