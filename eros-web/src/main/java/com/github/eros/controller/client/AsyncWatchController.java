package com.github.eros.controller.client;

import com.github.eros.async.Constants;
import com.github.eros.common.constant.HttpConstants;
import com.github.eros.common.model.Result;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Collection;

@RequestMapping("/async")
@RestController
public class AsyncWatchController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    /**
     * guava中的Multimap，多值map,对map的增强，一个key可以保持多个value
     */
    private Multimap<String, DeferredResult<Result<Void>>> watchRequests = Multimaps.synchronizedSetMultimap(HashMultimap.create());

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
                watchRequests.remove(namespace, deferredResult);
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
                watchRequests.remove(namespace, deferredResult);
            }
        });
        watchRequests.put(namespace, deferredResult);
        logger.info("Servlet thread released");
        return deferredResult;
    }


    /**
     * 发布namespace配置
     * @param namespace
     * @return
     */
    @Async(Constants.ASYNC_EXECUTOR)
    @RequestMapping(value = "/publish/{namespace}", method = RequestMethod.GET, produces = {"application/json"})
    public Result publishConfig(@PathVariable("namespace") String namespace) {
        if (watchRequests.containsKey(namespace)) {
            Collection<DeferredResult<Result<Void>>> deferredResults = watchRequests.get(namespace);
            long time = System.currentTimeMillis();
            //通知所有watch这个namespace变更的长轮训配置变更结果
            for (DeferredResult<Result<Void>> deferredResult : deferredResults) {
                Result<Void> result = Result.createSuccess();
                result.setMsgCode(HttpConstants.HttpStatus.CONTENT_MODIFIED.getCode());
                result.setMsgInfo(HttpConstants.HttpStatus.CONTENT_MODIFIED.getCode());
                deferredResult.setResult(result);
            }
        }
        return Result.createSuccess();
    }

    private Long getTimeout(Long timout){
        if (null == timout) {
            return HttpConstants.LONG_PULL_TIMEOUT_LONG_VALUE;
        }
        return ((timout/100)*90);
    }

}