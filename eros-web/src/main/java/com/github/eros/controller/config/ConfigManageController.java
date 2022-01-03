package com.github.eros.controller.config;


import com.github.eros.cache.WatchResultCache;
import com.github.eros.common.constant.HttpConstants;
import com.github.eros.common.lang.HttpResult;
import com.github.eros.common.lang.Result;
import com.github.eros.common.model.Config;
import com.github.eros.server.event.ConfigModifyEvent;
import com.github.eros.server.service.manage.ConfigInfoManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.Collection;

/**
 * @author fankongqiumu
 * @Description
 * @date 2021/12/17 11:41
 */
@RequestMapping("/config/manage")
@RestController
public class ConfigManageController {

    @Autowired
    private WatchResultCache watchResultCache;

    @Autowired
    private ConfigInfoManageService configInfoManageService;

    /**
     * 发布配置
     * @param config
     * @return
     */
    @PostMapping(value = "/publish")
    public HttpResult<Void> publish(@RequestBody Config config) {
        configInfoManageService.publish(config);
        return HttpResult.createDefaultSuccess();
    }

    @Async("asyncEventTaskExecutor")
    @EventListener
    @Order(1)
    public void configModifyEventListener(ConfigModifyEvent configModifyEvent){
        String namespace = configModifyEvent.getNamespace();
        if (watchResultCache.containNamespace(namespace)) {
            Collection<DeferredResult<Result<Void>>> deferredResults = watchResultCache.getByNameSpace(namespace);
            // 通知所有watch这个namespace变更的长轮训配置变更结果
            for (DeferredResult<Result<Void>> deferredResult : deferredResults) {
                Result<Void> result = Result.createSuccess();
                result.setMsgCode(HttpConstants.HttpStatus.CONTENT_MODIFIED.getCode());
                result.setMsgInfo(HttpConstants.HttpStatus.CONTENT_MODIFIED.getCode());
                deferredResult.setResult(result);
            }
        }
    }

}
