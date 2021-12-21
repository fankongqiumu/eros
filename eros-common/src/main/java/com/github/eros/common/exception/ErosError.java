package com.github.eros.common.exception;

/**
 * @author fankongqiumu
 * @description 异常类型
 * @date 2021/12/18 23:34
 */
public class ErosError {
    public static final ErosError PARAMS_ERROR = new ErosError("001", "Param %s is invalid");
    public static final ErosError SYSTEM_ERROR = new ErosError("002", "%s");
    public static final ErosError BUSINIESS_ERROR = new ErosError("003", "%s");

    public ErosError(String code, String message) {
        this.code = code;
        this.message = message;
    }

    private String code;
    private String message;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
