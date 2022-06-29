package com.github.eros.starter.runner;

import com.github.eros.client.Client;
import com.github.eros.client.Eros;
import com.github.eros.client.forest.Address;
import com.github.eros.client.step.ClientStartupStep;
import com.github.eros.common.constant.Constants;
import com.github.eros.starter.annotation.ErosStartupStep;
import org.springframework.beans.BeansException;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author fankongqiumu
 * @description spring容器初始化后启动client
 * @date 2021/12/17 19:39
 */
@Order(Ordered.LOWEST_PRECEDENCE - 10)
public class ErosRunner implements ApplicationRunner, ApplicationContextAware, ResourceLoaderAware {

    private ApplicationContext applicationContext;

    private ResourceLoader resourceLoader;

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
        List<Address> addressList = null;
        if (!CollectionUtils.isEmpty(nameserverDomainOptionValues)){
            addressList = new ArrayList<>(nameserverDomainOptionValues.size());
            Eros.parseAddress(addressList, nameserverDomainOptionValues);
        }
        return CollectionUtils.isEmpty(addressList)
                ? Eros.getNameServerDomains(resourceLoader.getClassLoader())
                : addressList;
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }
}
