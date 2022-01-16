package com.github.eros.demo.springboot;

import com.github.eros.client.step.ClientStartupStep;
import com.github.eros.starter.annotation.ErosStartupStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.Introspector;

@ErosStartupStep
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
