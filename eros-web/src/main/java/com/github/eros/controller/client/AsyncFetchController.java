package com.github.eros.controller.client;

import com.github.eros.common.lang.MD5;
import com.github.eros.common.model.Result;
import com.github.eros.common.util.JsonUtils;
import com.github.eros.controller.user.UserInfoHolder;
import com.github.eros.domain.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class AsyncFetchController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @RequestMapping(value = "/fetch/{namespace}", method = RequestMethod.GET)
    public Result<Config> fetch(@PathVariable("namespace") String namespace, HttpServletRequest request) {
        Config config = new Config();
        config.setData(JsonUtils.toJsonString(UserInfoHolder.getAll().values()));
        config.setLastModified(System.currentTimeMillis());
        config.setCheckMd5(MD5.getInstance().getMD5(config.getData()));
        return Result.createSuccessWithData(config);
    }

}