package com.github.eros.client.init;

/**
 * @author fankongqiumu
 * @description
 * @date 2021/12/17 15:43
 */
final class InitContextStartupStep extends ClientStartupStep {

    @Override
    public void start() {
        this.stepState = StepState.EXECUTEING;
    }

    /**
     * 此处不直接使用 HIGHEST_PRECEDENCE
     * 是因为想在此处保留扩展点，otherClientStartupStep.getOrder() < this.getOrder()
     * 将在本阶段开始之前执行
     * @return 初始化顺序
     */
    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE + 10;
    }
}
