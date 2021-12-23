package com.github.eros.async.facade;

import com.github.eros.async.Constants;
import com.github.eros.common.model.Result;
import com.github.eros.common.util.JsonUtils;
import com.github.eros.server.service.ConfigInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.servlet.AsyncContext;
import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @author fankongqiumu
 * @description
 * @date 2021/12/22 11:39
 */
@Service
public class AsyncService {

    private static final Logger logger = LoggerFactory.getLogger(AsyncService.class);

    @Autowired
    private ConfigInfoService configInfoService;


    @Async(Constants.ASYNC_EXECUTOR)
    public void asyncFetch(AsyncContext asyncContext) throws IOException {
        final ServletRequest request = asyncContext.getRequest();
        if (!request.isAsyncStarted()){
            request.startAsync();
        }
        final ServletInputStream input = request.getInputStream();
        input.setReadListener(new ReadListener() {
            @Override
            public void onDataAvailable() throws IOException {
            }

            @Override
            public void onAllDataRead() throws IOException {
                final ServletResponse response = asyncContext.getResponse();
                Result<Object> result = Result.createSuccess();
                String resultString = JsonUtils.toJsonString(result);
                response.getOutputStream().write(resultString.getBytes(StandardCharsets.UTF_8));
                asyncContext.complete();
            }

            @Override
            public void onError(Throwable t) {
                asyncContext.complete();
            }
        });
    }


    class AsyncRequestProcessor implements Runnable {

        private final AsyncContext asyncContext;

        public AsyncRequestProcessor(AsyncContext asyncContext) {
            this.asyncContext = asyncContext;
        }

        @Override
        public void run() {
        }
    }
}