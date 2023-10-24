package com.qiuguan.common.log.utils;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * @param <T>
 * @author zhanghw
 */
@Builder
@ToString
@Accessors(chain = true)
@AllArgsConstructor
public class MessageInfo<T> implements Serializable {

    private static final long serialVersionUID = 7162156649464891381L;
    @Getter
    @Setter
    private int code;
    @Getter
    @Setter
    private String messageInfo;
    @Getter
    @Setter
    private T data;

    public MessageInfo() {
        super();
    }

    public MessageInfo(T data) {
        super();
        this.data = data;
    }

    public MessageInfo(T data, String messageInfo) {
        super();
        this.data = data;
        this.messageInfo = messageInfo;
    }

    public MessageInfo(int code, String messageInfo) {
            super();
            this.code = code;
            this.messageInfo = messageInfo;
    }

    public MessageInfo(Throwable e) {
        super();
        this.messageInfo = e.getMessage();
        this.code = 400;
    }
}
