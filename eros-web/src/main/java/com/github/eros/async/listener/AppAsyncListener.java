package com.github.eros.async.listener;

import com.github.eros.async.facade.AsyncService;
import com.github.eros.common.model.Result;
import com.github.eros.common.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @author fankongqiumu
 * @description
 * @date 2021/12/22 16:14
 */
public class AppAsyncListener implements AsyncListener {

    private static final Logger logger = LoggerFactory.getLogger(AsyncService.class);

    @Override
    public void onComplete(AsyncEvent event) throws IOException {
        final AsyncContext asyncContext = event.getAsyncContext();
        ServletRequest request = asyncContext.getRequest();
        long timeout = asyncContext.getTimeout();
        logger.error("servlet async execute onComplete timeout:{}, request:{}", timeout, request);
    }

    @Override
    public void onTimeout(AsyncEvent event) throws IOException {
        AsyncContext context = event.getAsyncContext();
        ServletResponse response = context.getResponse();
        Result<Object> result = Result.createSuccess();
        result.setData("nil");
        String resultString = JsonUtils.toJsonString(result);
        response.getOutputStream().write(resultString.getBytes(StandardCharsets.UTF_8));
        context.complete();
    }

    @Override
    public void onError(AsyncEvent event) throws IOException {
        Throwable throwable = event.getThrowable();
        final AsyncContext asyncContext = event.getAsyncContext();
        ServletRequest request = asyncContext.getRequest();
        logger.error("servlet async execute error:{}, request:{}", throwable, request);
        asyncContext.complete();
    }

    @Override
    public void onStartAsync(AsyncEvent event) throws IOException {
        final AsyncContext asyncContext = event.getAsyncContext();
        ServletRequest request = asyncContext.getRequest();
        long timeout = asyncContext.getTimeout();
        logger.error("servlet async execute onStartAsync timeout:{}, request:{}", timeout, request);
    }
}
