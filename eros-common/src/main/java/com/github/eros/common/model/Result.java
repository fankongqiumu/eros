package com.github.eros.common.model;

import com.github.eros.common.constant.HttpConstants;

import java.io.Serializable;

/**
 * @author fankongqiumu
 * @description 统一返回
 * @date 2021/12/17 22:12
 */
public class Result<Target> implements Serializable {
    private static final long serialVersionUID = 6901051158611202215L;

    private Target data;

    private boolean success = true;

    /**
     * 异常栈信息——某些控制台调试接口会透出
     */
    private String exceptionStack;

    private String msgCode;

    private String msgInfo;

    /**
     * 兼容依赖msgCode 的接口, 如mtop的错误码
     *
     */
    public String getMsgCode() {
        return this.msgCode;
    }

    /**
     * 兼容依赖msgInfo的接口, 如mtop的错误信息
     *
     */
    public String getMsgInfo() {
        return this.msgInfo;
    }

    public void setSuccessTrue() {
        this.success = true;
    }

    public Target getData() {
        return data;
    }

    public void setData(Target data) {
        this.data = data;
    }

    /**
     * 泛型方式创建Result比较易用，建议不要直接使用默认的构造方法
     */
    public static <Target> Result<Target> create() {
        return new Result<Target>();
    }

    /**
     * 根据返回的数据构建Result
     */
    public static <Target> Result<Target> createWithData(Target data) {
        Result<Target> result = Result.create();
        result.setData(data);
        return result;
    }

    public static <Target> Result<Target> createSuccess() {
        Result<Target> result = new Result<Target>();
        result.setSuccessTrue();
        return result;
    }

    /**
     * 根据返回的数据构建Result
     */
    public static <Target> Result<Target> createSuccessWithData(Target data) {
        Result<Target> result = Result.create();
        result.setSuccessTrue();
        result.setData(data);
        return result;
    }

    public static <Target> Result<Target> createFailWith(String errorCode, String errorMsg) {
        Result<Target> result = Result.create();
        result.setSuccess(false);
        result.msgInfo = errorMsg;
        result.msgCode = errorCode;
        return result;
    }

    public void setMsgCode(String msgCode) {
        this.msgCode = msgCode;
    }

    public void setMsgInfo(String msgInfo) {
        this.msgInfo = msgInfo;
    }

    public String getExceptionStack() {
        return exceptionStack;
    }

    public void setExceptionStack(String exceptionStack) {
        this.exceptionStack = exceptionStack;
    }

    public boolean isSuccess() {
        return success;
    }

    public boolean isNotSuccess() {
        return !success;
    }


    public void setSuccess(boolean success) {
        this.success = success;
    }

    @Override
    public String toString() {
        return "[success="        + isSuccess()         + "," +
                "msgCode="        + getMsgCode()        + "," +
                "msgInfo="        + getMsgInfo()        + "," +
                "exceptionStack=" + getExceptionStack() + "," +
                "data="           + getData()           +
                "]";
    }
}
