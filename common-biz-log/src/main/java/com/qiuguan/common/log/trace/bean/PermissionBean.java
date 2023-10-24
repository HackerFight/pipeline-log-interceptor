package com.qiuguan.common.log.trace.bean;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author fu yuan hui
 */
@Data
public class PermissionBean implements Serializable {

    private Integer permissionId;

    private String permissionCode;

    private String permissionName;
    
    private String helpDocumentName;

    private String helpDocumentUrl;

    /**
     * 前段链接
     */
    private String path;

    /**
     * vue链接
     */
    private String url;

    private String permission;

    private String icon;

    private Integer parentId;

    private String parentIds;

    private Integer sort;

    /**
     * 1:启用 0:停用
     */
    private Integer status;

    private String permissionType;

    private Integer creatorId;

    private LocalDateTime createTime;

    private LocalDateTime stopDate;

    private String appCode;
    
    private String disabled;

    private String adminDefault;

    private Integer customized;

    private Integer isFilter;

    private String mappingUrl;

    private Integer urlCrud;

}