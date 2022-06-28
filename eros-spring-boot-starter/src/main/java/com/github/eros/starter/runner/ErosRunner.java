package com.github.eros.starter.runner;

import com.github.eros.client.Client;
import com.github.eros.client.Eros;
import com.github.eros.client.forest.Address;
import com.github.eros.client.step.ClientStartupStep;
import com.github.eros.common.constant.Constants;
import com.github.eros.starter.annotation.ErosStartupStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * spring容器初始化后启动client
 */
@Order
@Component
public class ErosRunner implements ApplicationRunner {

    @Autowired
    private ApplicationContext applicationContext;
    
    @Override
    public void run(ApplicationArguments args) {
        Client client = Eros.getInstance(innerGetNameServerDomains(args));
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

    private List<Address> innerGetNameServerDomains(ApplicationArguments args){
        // 优先选项参数
        List<String> nameserverDomainOptionValues = args.getOptionValues(Constants.PropertyConstants.NAME_SERVER_DOMAINS);
        if (!CollectionUtils.isEmpty(nameserverDomainOptionValues)){
            List<Address> addressList = new ArrayList<>(nameserverDomainOptionValues.size());
            Eros.parseAddress(addressList, nameserverDomainOptionValues);
            return addressList;
        }
        return Eros.getNameServerDomains();
    }
}
