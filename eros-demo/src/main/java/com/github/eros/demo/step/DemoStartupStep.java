package com.github.eros.demo.step;

import com.github.eros.client.step.ClientStartupStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.Introspector;

public class DemoStartupStep extends ClientStartupStep {
    private  final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public void start() {
        logger.info("start customer step [{}]...", Introspector.decapitalize(this.getClass().getSimpleName()));
    }
}
