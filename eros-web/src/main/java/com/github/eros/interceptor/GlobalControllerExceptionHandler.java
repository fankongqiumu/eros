package com.github.eros.interceptor;

import com.github.eros.common.lang.Result;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;

@RestControllerAdvice
public class GlobalControllerExceptionHandler {

    @ExceptionHandler(value = {AsyncRequestTimeoutException.class})
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Result<String> noHandlerFoundException(Exception ex) {
        return Result.createSuccessWithData(HttpStatus.NO_CONTENT.getReasonPhrase());
    }
}
