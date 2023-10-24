package com.qiuguan.common.log.trace.logbiz.entity;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import java.io.Serializable;

/**
 * 业务日志需要忽略的url表(ServicesLogSkipUrls)表实体类
 *
 * @author makejava
 * @since 2023-08-14 11:32:52
 */
@SuppressWarnings("serial")
public class ServicesLogSkipUrls extends Model<ServicesLogSkipUrls> {
    //pk
    private Long id;
    //需要忽略的url
    private String url;
    //说明
    private String mark;
    //拓展
    private String ext;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMark() {
        return mark;
    }

    public void setMark(String mark) {
        this.mark = mark;
    }

    public String getExt() {
        return ext;
    }

    public void setExt(String ext) {
        this.ext = ext;
    }

    /**
     * 获取主键值
     *
     * @return 主键值
     */
    @Override
    protected Serializable pkVal() {
        return this.id;
    }
    }

