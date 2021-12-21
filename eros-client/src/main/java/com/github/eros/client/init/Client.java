package com.github.eros.client.init;

import com.github.eros.common.exception.ErosError;
import com.github.eros.common.exception.ErosException;
import com.github.eros.common.order.OrderComparator;
import com.github.eros.common.step.StartupStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author fankongqiumu
 * @description
 * @date 2021/12/17 15:28
 */
final class Client {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final List<StartupStep> CLIENT_INIT_STARTUP_STEP_HOLDER = new ArrayList<>(16);

    private Client(){}
    private static final Client INSTANCE = new Client();

    private static volatile boolean isBuild = false;

    static class ClientBuilder {

         void step (StartupStep startupStep) {
            if (Objects.isNull(startupStep)) {
                return;
            }
            CLIENT_INIT_STARTUP_STEP_HOLDER.add(startupStep);
        }

        Client build(){
            if (isBuild){
                throw new RuntimeException("the client already build!");
            }
            if (CLIENT_INIT_STARTUP_STEP_HOLDER.isEmpty()) {
                throw new RuntimeException("startup step isEmpty...");
            }
            isBuild = true;
            return INSTANCE;
        }
    }

    void start() {
        if (!isBuild){
            throw new ErosException(ErosError.SYSTEM_ERROR, "the client not init!");
        }
        CLIENT_INIT_STARTUP_STEP_HOLDER.sort(OrderComparator.INSTANCE);
        CLIENT_INIT_STARTUP_STEP_HOLDER.forEach((startupStep -> {
            String stepName = startupStep.getName();
            logger.info("step-{} is starting...", stepName);
            startupStep.start();
            startupStep.end();
            logger.info("step-{} is started...", stepName);
        }));
    }
}
