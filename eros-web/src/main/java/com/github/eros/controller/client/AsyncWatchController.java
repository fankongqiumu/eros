package com.github.eros.controller.client;

import com.github.eros.cache.WatchResultCache;
import com.github.eros.common.constant.HttpConstants;
import com.github.eros.common.lang.Result;
import com.github.eros.server.service.ConfigInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.function.Consumer;

@RequestMapping("/async")
@RestController
public class AsyncWatchController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private WatchResultCache watchResultCache;

    @Autowired
    private ConfigInfoService configInfoService;

    /**
     * 长轮询
     *
     * @param namespace
     * @param request
     * @return
     */
    @GetMapping("/watch/{namespace}")
    public DeferredResult<Result<Void>> watch(@PathVariable("namespace") String namespace,
                                              @RequestParam("lastModified") Long lastModified,
                                              @RequestParam(name = "timeout", required = false) Long timeout,
                                              HttpServletRequest request) {
        DeferredResult<Result<Void>> deferredResult;
        boolean hasEffectiveModifySyncEvent = configInfoService.hasEffectiveModifySyncEvent(namespace, lastModified);
        if (hasEffectiveModifySyncEvent) {
            // 存在没有监听到的变更
            Result<Void> result = Result.createSuccess();
            result.setMsgCode(HttpConstants.HttpStatus.CONTENT_MODIFIED.getCode());
            result.setMsgInfo(HttpConstants.HttpStatus.CONTENT_MODIFIED.getCode());
            deferredResult = new DeferredResult<>();
            deferredResult.setResult(result);
            return deferredResult;
        }

        deferredResult = new DeferredResult<>(getTimeout(timeout));
        //当deferredResult完成时（不论是超时还是异常还是正常完成），移除watchRequests中相应的watch key
        deferredResult.onCompletion(new Runnable() {
            @Override
            public void run() {
                logger.info("remove key:[{}] ", namespace);
                watchResultCache.remove(namespace, deferredResult);
            }
        });

        deferredResult.onError(new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) {
                logger.error("watch [{}] error:[{}]", namespace, throwable);
                Result<Void> noContent = Result.createSuccess();
                noContent.setMsgCode(String.valueOf(HttpConstants.HttpStatus.SERVER_BUSY.getCode()));
                noContent.setMsgInfo(HttpConstants.HttpStatus.SERVER_BUSY.getReasonPhrase());
                deferredResult.setResult(noContent);
                watchResultCache.remove(namespace, deferredResult);
            }
        });

        deferredResult.onTimeout(new Runnable() {
            @Override
            public void run() {
                logger.info("watch [{}] Timeout:{}", namespace, LocalDateTime.now());
                Result<Void> noContent = Result.createSuccess();
                noContent.setMsgCode(String.valueOf(HttpConstants.HttpStatus.NOT_MODIFIED.getCode()));
                noContent.setMsgInfo(HttpConstants.HttpStatus.NOT_MODIFIED.getReasonPhrase());
                deferredResult.setResult(noContent);
                watchResultCache.remove(namespace, deferredResult);
            }
        });

        watchResultCache.add(namespace, deferredResult);
        return deferredResult;
    }

    private Long getTimeout(Long timout) {
        if (null == timout) {
            return HttpConstants.LONG_PULL_TIMEOUT_LONG_VALUE;
        }
        return ((timout / 100) * 90);
    }

}