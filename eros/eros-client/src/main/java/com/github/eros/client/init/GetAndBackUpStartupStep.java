package com.github.eros.client.init;

import com.github.eros.client.listener.ErosClientListener;

import java.util.Collection;

/**
 * @author fankongqiumu
 * @description 本地备份
 * @date 2021/12/17 15:54
 */
final class GetAndBackUpStartupStep extends ClientStartupStep {

    @Override
    public void start() {
        this.stepState = StepState.EXECUTEING;
        Collection<ErosClientListener> listeners = ErosClientListener.getListeners();
        logger.info("......[step-{}] eros listeners: {}......",this.getName(), listeners);
        if (listeners.isEmpty()) {
            logger.warn("......[step-{}] no eros listeners......",this.getName());
            ErosClientListener.nonListenerCallback();
            return;
        }
        listeners.forEach(ErosClientListener::fetchAtFixedRate);
    }

    /**
     * 此处不直接使用 HIGHEST_PRECEDENCE
     * 是因为想在此处保留扩展点，otherClientStartupStep.getOrder() < this.getOrder()
     * 将在本阶段开始之前执行
     * @return 初始化顺序
     */
    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE + 50;
    }
}
