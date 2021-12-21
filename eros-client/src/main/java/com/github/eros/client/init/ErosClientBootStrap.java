package com.github.eros.client.init;

import com.github.eros.common.exception.ErosError;
import com.github.eros.common.exception.ErosException;
import com.github.eros.common.step.BootStrap;
import com.github.eros.common.step.StartupStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author fankongqiumu
 * @description client 启动引导
 * @date 2021/12/17 14:32
 */
public class ErosClientBootStrap implements BootStrap {

    private static final Logger logger = LoggerFactory.getLogger(ErosClientBootStrap.class);

    private final Client.ClientBuilder builder;

    private  final Client client;

    private static volatile boolean started = false;

    private static final Object lock = new Object();

    private static class ErosClientBootStrapHolder {
        private static final ErosClientBootStrap INSTANCE = new ErosClientBootStrap();
    }

    public static BootStrap newInstance() {
        return ErosClientBootStrapHolder.INSTANCE;
    }

    private ErosClientBootStrap() {
        builder = new Client.ClientBuilder();
        builder.step(new InitContextStartupStep());
        builder.step(new GetAndBackUpStartupStep());
        this.client = builder.build();
    }

    @Override
    public void addCustomSteps(List<StartupStep> customSteps) {
        if (started) {
            throw new ErosException(ErosError.SYSTEM_ERROR, "eros client has started...");
        }
        if (null != customSteps && !customSteps.isEmpty()) {
            customSteps.forEach(builder::step);
        }
    }

    @Override
    public void start() {
        if (started) {
            logger.error("eros client has started...");
            return;
        }
        synchronized (lock) {
            client.start();
            started = true;
        }
        logger.info("eros client started...");
    }
}
