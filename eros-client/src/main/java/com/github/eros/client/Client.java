package com.github.eros.client;

import com.github.eros.client.step.ClientStartupStep;
import com.github.eros.common.constant.Contants;
import com.github.eros.common.exception.ErosError;
import com.github.eros.common.exception.ErosException;
import com.github.eros.common.order.OrderComparator;
import com.github.eros.common.step.StartupStep;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


/**
 * @author fankongqiumu
 * @description
 * @date 2021/12/17 21:28
 */
public final class Client {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final Map<String, StartupStep> CUSTOMER_INIT_STEP_HOLDER = new HashMap<>(Contants.COLLECTION_DEFAULT_INITIAL_CAPACITY);

    private static final Map<String, StartupStep> INNER_STEP_HOLDER = new HashMap<>(Contants.COLLECTION_DEFAULT_INITIAL_CAPACITY);

    public static volatile boolean started = false;

    private static final Object lock = new Object();

    private final Long beginTime;

    private Client(){
        beginTime = System.currentTimeMillis();
        initInnerStep();
    }

    private static class ErosClientHolder {
        private static final Client INSTANCE = new Client();
    }

    static Client newInstance() {
        return ErosClientHolder.INSTANCE;
    }

    private void initInnerStep(){
        InitContextStartupStep initContextStartupStep = new InitContextStartupStep();
        INNER_STEP_HOLDER.put(initContextStartupStep.getName(), initContextStartupStep);
        addCustomSteps(Collections.singletonList(new FetchAndWatchStartupStep()));
    }

    /**
     * 在client start 之前，用户可以通过此方法添加自定义的启动步骤
     * @param customSteps
     */
    public void addCustomSteps(List<ClientStartupStep> customSteps) {
        if (started) {
            throw new ErosException(ErosError.SYSTEM_ERROR, "the client already started...");
        }
        if (null != customSteps && !customSteps.isEmpty()) {
            customSteps.forEach(this::step);
        }
    }

    private void step(StartupStep startupStep) {
        if (null == startupStep){
            throw new ErosException(ErosError.PARAMS_ERROR, "[startupStep]");
        }
        String stepName = startupStep.getName();
        if (StringUtils.isBlank(stepName)){
            throw new ErosException(ErosError.BUSINIESS_ERROR, "step:[" + startupStep.getClass().getName() + "] stepName is empty...");
        }
        if (INNER_STEP_HOLDER.containsKey(stepName) || CUSTOMER_INIT_STEP_HOLDER.containsKey(stepName)){
            throw new ErosException(ErosError.BUSINIESS_ERROR, "step:[" + stepName + "]  already exist...");
        }
        synchronized (lock) {
            if (!CUSTOMER_INIT_STEP_HOLDER.containsKey(stepName)){
                CUSTOMER_INIT_STEP_HOLDER.put(stepName, startupStep);
            }
        }
    }

    public void start() {
        if (started){
            logger.error("the client already started!");
            return;
        }
        synchronized (lock) {
            if (!started) {
                logger.info("client init...");
                stepBatchStart(new ArrayList<>(INNER_STEP_HOLDER.values()));
                stepBatchStart(new ArrayList<>(CUSTOMER_INIT_STEP_HOLDER.values()));
                started = true;
                logger.info("the client started in(ms) {}...", System.currentTimeMillis() - beginTime);
            }
        }
    }

    static boolean isStarted(){
        return started;
    }

    static Collection<ErosClientListener> getListeners(){
        return ErosClientListener.getListeners();
    }

    private void stepBatchStart(List<StartupStep> steps){
        steps.sort(OrderComparator.INSTANCE);
        for (StartupStep step : steps) {
            String stepName = step.getName();
            logger.info("step-{} is starting...", stepName);
            step.start();
            step.end();
            logger.info("step-{} is started...", stepName);
        }
    }

    private static final class InitContextStartupStep extends ClientStartupStep {

        @Override
        public void start() {
            this.stepState = StepState.EXECUTEING;
        }

        @Override
        public int getOrder() {
            return HIGHEST_PRECEDENCE;
        }
    }


    /**
     * 将此阶段注册成 CUSTOMER_INIT_STEP, 结合 getOrder(),
     * 可以在此处提供一个扩展点，当customerClientStartupStep.getOrder() < this.getOrder()
     * 将在本阶段开始之前执行
     */
    private static final class FetchAndWatchStartupStep extends ClientStartupStep {

        @Override
        public void start() {
            this.stepState = StepState.EXECUTEING;
            Collection<ErosClientListener> listeners = getListeners();
            logger.info("......[step-{}] listeners: {}......",this.getName(), listeners);
            if (listeners.isEmpty()) {
                logger.warn("......[step-{}] no listeners......",this.getName());
                ErosClientListener.nonListenerCallback();
                return;
            }
            listeners.forEach(ErosClientListener::fetchAndWatch);
        }

        @Override
        public int getOrder() {
            return HIGHEST_PRECEDENCE  + 50;
        }
    }

}
