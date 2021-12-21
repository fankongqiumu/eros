package com.github.eros.common.exception;

import java.util.Objects;

/**
 * @author fankongqiumu
 * @description
 * @date 2021/12/18 23:31
 */
public class ErosException extends RuntimeException {

    private ErosError erosError;
    private String bizErrorMessage;

    public ErosException(ErosError erosError, String bizErrorMessage, Throwable cause) {
        super(cause.getMessage(), cause);
        Objects.requireNonNull(erosError);
        Objects.requireNonNull(cause);
        Objects.requireNonNull(bizErrorMessage);
        this.erosError = erosError;
        this.bizErrorMessage = bizErrorMessage;
    }

    public ErosException(ErosError erosError, String bizErrorMessage){
        super(bizErrorMessage);
        Objects.requireNonNull(erosError);
        Objects.requireNonNull(bizErrorMessage);
        this.erosError = erosError;
        this.bizErrorMessage = bizErrorMessage;
    }

    public ErosError getErosError() {
        return erosError;
    }

    public void setErosError(ErosError erosError) {
        this.erosError = erosError;
    }

    public ErosError error() {
        return getErosError();
    }

    public String getErrorCode() {
        return getErosError().getCode();
    }

    public String getErrorMessage() {
        return String.format(getErosError().getMessage(), this.getBizErrorMessage());
    }

    public String getBizErrorMessage() {
        return bizErrorMessage;
    }

    public void setBizErrorMessage(String bizErrorMessage) {
        this.bizErrorMessage = bizErrorMessage;
    }
}
