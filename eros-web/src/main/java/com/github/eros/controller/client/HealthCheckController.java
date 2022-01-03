package com.github.eros.controller.client;

import com.github.eros.common.constant.HttpConstants;
import com.github.eros.server.cache.ConfigLocalCache;
import com.github.nameserver.constant.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class HealthCheckController {
    private static final Logger logger = LoggerFactory.getLogger(ConfigLocalCache.class);

    @GetMapping(Constants.UriConstants.HEALTH_CHECK_PATH)
    public String healthCheckController(HttpServletRequest httpServletRequest){
        String remoteAddr = httpServletRequest.getRemoteAddr();
        String remoteHost = httpServletRequest.getRemoteHost();
        logger.info("healthCheck done from {}...", remoteHost);
        return HttpConstants.HttpStatus.OK.getReasonPhrase();
    }
}
