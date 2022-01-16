package com.github.eros.client;

import java.util.Collection;

public class Eros {

    /**
     * 获取client实例
     * @return
     */
    public static Client getInstance(){
        return Client.newInstance();
    }

    /**
     * 添加 ErosClientListener
     * @param listener
     */
    public static void addListener(ErosClientListener listener){
        if (isStarted()){
            // 允许client启动后添加, 会自动拉取一次配置并长轮训监听
            synchronized (Eros.class){
                listener.fetchAndWatch();
            }
        }
    }

    /**
     * 获取当前所有的ErosClientListener
     * @return
     */
    public static Collection<ErosClientListener> getListeners(){
        return Client.getListeners();
    }

    /**
     *
     * @return client 是否已经启动
     */
    public static boolean isStarted(){
        return Client.isStarted();
    }
}
