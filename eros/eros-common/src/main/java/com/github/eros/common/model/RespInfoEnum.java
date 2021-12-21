package com.github.eros.common.model;

/**
 * @author fankongqiumu
 * @description
 * @date 2021/12/19 21:54
 */
public enum RespInfoEnum {
    SUCCESS("200", MessageType.TEXT, "success"),
    ERROR("500", MessageType.JSON, "error")
    ;
    private String code;
    private MessageType messageType;
    private String message;

    private RespInfoEnum(String code, MessageType messageType, String message) {
        this.code =code;
        this.messageType = messageType;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
