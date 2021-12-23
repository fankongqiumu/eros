package com.github.eros.controller;

import com.github.eros.common.constant.HttpConstants;
import com.github.eros.common.model.Result;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

@RequestMapping("/async")
@RestController
public class AsyncRequestController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    //guava中的Multimap，多值map,对map的增强，一个key可以保持多个value
    private Multimap<String, DeferredResult<Result<Void>>> watchRequests = Multimaps.synchronizedSetMultimap(HashMultimap.create());

    //长轮询
    @RequestMapping(value = "/watch/{namespace}", method = RequestMethod.GET)
    public DeferredResult<Result<Void>> watch(@PathVariable("namespace") String namespace, HttpServletRequest request) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        DeferredResult<Result<Void>> deferredResult;
        String timeout = request.getHeader("timeout");
        String requestId = request.getHeader("requestId");
        if (!StringUtils.isNumeric(timeout) || StringUtils.isBlank(requestId)){
            deferredResult = new DeferredResult<>();
            Result<Void> noTimeoutHeader = Result.createFailWith("500", "no timeout or requestId header");
            deferredResult.setErrorResult(noTimeoutHeader);
            return deferredResult;
        }
        logger.info("{} watch config on:{}", requestId, format.format(new Date()));
        logger.info("Request received");
        long timeoutLong = Long.parseLong(timeout);
        timeoutLong = ((timeoutLong/100)*90);
        deferredResult = new DeferredResult<>(timeoutLong);
        //当deferredResult完成时（不论是超时还是异常还是正常完成），移除watchRequests中相应的watch key
        deferredResult.onCompletion(new Runnable() {
            @Override
            public void run() {
                System.out.println("remove key:" + namespace);
                watchRequests.remove(namespace, deferredResult);
            }
        });

        deferredResult.onTimeout(new Runnable() {
            @Override
            public void run() {
                logger.info("[{}] watch config Timeout:{}", requestId, format.format(new Date()));
                System.out.println("key:" + namespace + "Timeout");
                Result<Void> noContent = Result.createSuccess();
                noContent.setMsgCode(String.valueOf(HttpConstants.HttpStatus.NOT_MODIFIED.getCode()));
                noContent.setMsgInfo(HttpConstants.HttpStatus.NOT_MODIFIED.getReasonPhrase());
                deferredResult.setResult(noContent);
                watchRequests.remove(namespace, deferredResult);
            }
        });
        watchRequests.put(namespace, deferredResult);
        logger.info("Servlet thread released");
        return deferredResult;
    }

    @Async
    //发布namespace配置
    @RequestMapping(value = "/publish/{namespace}", method = RequestMethod.GET)
    public Result publishConfig(@PathVariable("namespace") String namespace) {
        if (watchRequests.containsKey(namespace)) {
            Collection<DeferredResult<Result<Void>>> deferredResults = watchRequests.get(namespace);
            long time = System.currentTimeMillis();
            //通知所有watch这个namespace变更的长轮训配置变更结果
            for (DeferredResult<Result<Void>> deferredResult : deferredResults) {
                Result<Void> result = Result.createSuccess();
                result.setMsgCode(HttpConstants.HttpStatus.CONTENT_MODIFIED.getCode());
                result.setMsgInfo(HttpConstants.HttpStatus.CONTENT_MODIFIED.getCode());
            }
        }
        return Result.createSuccess();
    }

}