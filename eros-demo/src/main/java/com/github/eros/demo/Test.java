package com.github.eros.demo;

import com.github.eros.client.Client;
import com.github.eros.client.Eros;
import com.github.eros.client.ErosClientListener;
import com.github.eros.client.step.ClientStartupStep;
import com.google.common.collect.Lists;

/**
 * @author fankongqiumu
 * @description 普通项目使用方式
 * @date 2021/12/19 21:24
 */
public class Test {
    public static void main(String[] args) {
//        可通过此方式添加自己的listener， 或者使用eros.facade文件描述也可
//        Eros.addListener(new ErosClientListener() {
//            @Override
//            public String getApp() {
//                return null;
//            }
//
//            @Override
//            public String getNamespace() {
//                return null;
//            }
//
//            @Override
//            public String getGroup() {
//                return null;
//            }
//
//            @Override
//            protected void onReceiveConfigInfo(String configData) {
//
//            }
//        });
        Client client = Eros.getInstance();
//        通过此方式添加自定义的 ClientStartupStep
//        client.addCustomSteps(Lists.newArrayList(new ClientStartupStep(){
//
//            @Override
//            public void start() {
//
//            }
//
//            @Override
//            public int getOrder() {
//                return 0;
//            }
//        }));
        client.start();
        for (;;){

        }
    }
}
