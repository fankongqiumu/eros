package com.github.eros.starter.runner;

import com.github.eros.client.Client;
import com.github.eros.client.Eros;
import com.github.eros.client.step.ClientStartupStep;
import com.github.eros.starter.annotation.ErosStartupStep;
import org.springframework.beans.BeansException;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.Order;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * spring容器初始化后启动client
 */
@Order
public class ErosRunner implements ApplicationRunner, ApplicationContextAware {

    private ApplicationContext applicationContext;
    
    @Override
    public void run(ApplicationArguments args) throws Exception {
        Client client = Eros.getInstance();
        // 注册自定义的启动阶段
        Map<String, Object> erosStartupSteps = applicationContext.getBeansWithAnnotation(ErosStartupStep.class);
        if (!erosStartupSteps.isEmpty()){
            List<ClientStartupStep> startupStepList = erosStartupSteps.values().stream()
                    .map(ClientStartupStep.class::cast)
                    .collect(Collectors.toList());
            client.addCustomSteps(startupStepList);
        }
        client.start();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;   
    }
}
