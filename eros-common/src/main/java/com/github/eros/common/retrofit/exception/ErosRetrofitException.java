package com.github.eros.common.retrofit.exception;

import com.github.eros.common.exception.ErosError;
import com.github.eros.common.exception.ErosException;

/**
 * @author fankongqiumu
 * @description
 * @date 2021/12/19 00:02
 */
public class ErosRetrofitException extends ErosException {
    public ErosRetrofitException(ErosError erosError, String bizErrorMessage, Throwable cause) {
        super(erosError, bizErrorMessage, cause);
    }

    public ErosRetrofitException(ErosError erosError, String bizErrorMessage) {
        super(erosError, bizErrorMessage);
    }

}
