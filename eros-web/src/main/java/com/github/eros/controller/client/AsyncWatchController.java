package com.github.eros.controller.client;

import com.github.eros.cache.WatchResultCache;
import com.github.eros.common.constant.HttpConstants;
import com.github.eros.common.model.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@RequestMapping("/async")
@RestController
public class AsyncWatchController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private WatchResultCache watchResultCache;

    /**
     * 长轮询
     * @param namespace
     * @param request
     * @return
     */
    @RequestMapping(value = "/watch/{namespace}", method = RequestMethod.GET)
    public DeferredResult<Result<Void>> watch(@PathVariable("namespace") String namespace,
                                              @RequestParam("clientKey") String clientKey,
                                              @RequestParam(name = "timeout", required = false) Long timeout,
                                              HttpServletRequest request) {

        DeferredResult<Result<Void>> deferredResult;
        logger.info("{} watch {} on:{}", clientKey, namespace, LocalDateTime.now());
        logger.info("Request received");
        deferredResult = new DeferredResult<>(getTimeout(timeout));
        //当deferredResult完成时（不论是超时还是异常还是正常完成），移除watchRequests中相应的watch key
        deferredResult.onCompletion(new Runnable() {
            @Override
            public void run() {
                logger.info("remove key:[{}] ", namespace);
                watchResultCache.remove(namespace, deferredResult);
            }
        });

        deferredResult.onTimeout(new Runnable() {
            @Override
            public void run() {
                logger.info("[{}] watch [{}] Timeout:{}", clientKey, namespace, LocalDateTime.now());
                Result<Void> noContent = Result.createSuccess();
                noContent.setMsgCode(String.valueOf(HttpConstants.HttpStatus.NOT_MODIFIED.getCode()));
                noContent.setMsgInfo(HttpConstants.HttpStatus.NOT_MODIFIED.getReasonPhrase());
                deferredResult.setResult(noContent);
                watchResultCache.remove(namespace, deferredResult);
            }
        });
        watchResultCache.add(namespace, deferredResult);
        logger.info("Servlet thread released");
        return deferredResult;
    }

    private Long getTimeout(Long timout){
        if (null == timout) {
            return HttpConstants.LONG_PULL_TIMEOUT_LONG_VALUE;
        }
        return ((timout/100)*90);
    }

}