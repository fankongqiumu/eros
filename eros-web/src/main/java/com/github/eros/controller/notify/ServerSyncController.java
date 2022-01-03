package com.github.eros.controller.notify;

import com.github.eros.common.lang.HttpResult;
import com.github.eros.server.service.manage.ConfigInfoManageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author fankongqiumu
 * @description
 * @date 2021/12/25 16:17
 */
@RequestMapping("/server")
@RestController
public class ServerSyncController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ConfigInfoManageService configInfoManageService;

    /**
     * todo 服务器鉴权
     * @param namespace
     * @return
     */
    @GetMapping("/sync/{namespace}")
    public HttpResult<Void> sync(@PathVariable("namespace") String namespace){
        configInfoManageService.receivePublish(namespace);
        return HttpResult.createDefaultSuccess();
    }

}
