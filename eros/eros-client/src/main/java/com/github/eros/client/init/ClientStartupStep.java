package com.github.eros.client.init;

import com.github.eros.common.step.StartupStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author fankongqiumu
 * @description
 * @date 2021/12/17 15:23
 */
abstract class ClientStartupStep implements StartupStep {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    protected StepState stepState = StepState.NOT_STARTED;

    @Override
    public StepState getState() {
        return stepState;
    }

    @Override
    public void end(){
        this.stepState = StepState.END;
        logger.info("......[step-{}] start success......", this.getName());
    }
}
