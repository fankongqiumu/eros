package com.github.eros.common.step;

import com.github.eros.common.order.Ordered;

import java.beans.Introspector;

/**
 * @author fankongqiumu
 * @description
 * @date 2021/12/17 15:16
 */
public interface StartupStep extends Ordered {

    StepState getState();

    default String getName() {
       return Introspector.decapitalize(this.getClass().getSimpleName());
    }

    void start();

    void end();

    enum StepState {
        NOT_STARTED,
        EXECUTEING,
        END,
    }
}
