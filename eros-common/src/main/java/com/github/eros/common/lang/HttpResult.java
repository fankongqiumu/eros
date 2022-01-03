package com.github.eros.common.lang;

import com.github.eros.common.constant.HttpConstants;

public class HttpResult<Target> extends Result<Target>{

    public static  HttpResult<Void> createDefaultSuccess() {
        HttpResult<Void> result = new HttpResult<>();
        result.setSuccessTrue();
        result.setMsgCode(HttpConstants.HttpStatus.OK.getCode());
        result.setMsgCode(HttpConstants.HttpStatus.OK.getReasonPhrase());
        return result;
    }
}
