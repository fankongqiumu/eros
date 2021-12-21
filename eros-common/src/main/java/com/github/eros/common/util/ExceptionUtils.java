package com.github.eros.common.util;

import com.github.eros.common.exception.ErosError;
import com.github.eros.common.exception.ErosException;
import com.github.eros.common.model.Result;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.UUID;

/**
 * @author fankongqiumu
 * @description
 * @date 2021/12/17 14:20
 */
public class ExceptionUtils {

    public static String getExceptionMsg(Throwable e) {
        try {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            return "\r\n" + sw.toString() + "\r\n";
        } catch (Exception e2) {
            return "bad getErrorInfoFromException";
        }
    }

    public static String generateTraceId() {
        return UUID.randomUUID().toString().replaceAll("-", "").toLowerCase();
    }

    /**
     * 公用异常处理
     * @param e
     * @param result
     */
    public static void dealException(Throwable e, Result<?> result) {
        result.setSuccess(false);
        if (e instanceof ErosException) {
            ErosException erosException = (ErosException) e;
            result.setMsgCode(erosException.getErrorCode());
            result.setMsgInfo(erosException.getErrorMessage());
            result.setExceptionStack(getExceptionMsg(erosException));
        }
        result.setMsgCode(ErosError.SYSTEM_ERROR.getCode());
        result.setMsgInfo(e.getMessage());
        result.setExceptionStack(getExceptionMsg(e));
    }
}
