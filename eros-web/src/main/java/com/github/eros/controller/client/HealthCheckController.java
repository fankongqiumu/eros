package com.github.eros.controller.client;

import com.github.eros.common.constant.HttpConstants;
import com.github.eros.server.cache.ConfigLocalCache;
import com.github.nameserver.constant.UriConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheckController {
    private static final Logger logger = LoggerFactory.getLogger(ConfigLocalCache.class);

    @GetMapping(UriConstants.HEALTH_CHECK_PATH)
    public String healthCheckController(){
        logger.info("nameserver healthCheck done...");
        return HttpConstants.HttpStatus.OK.getReasonPhrase();
    }
}
