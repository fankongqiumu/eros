package com.github.eros.common.lang;

/**
 * @author fankongqiumu
 * @description
 * @date 2021/12/20 15:29
 */
public class ClassScanner {

    public static final String FACTORIES_RESOURCE_LOCATION = "META-INF/listener.properties";

    public void loadClass(String packageName) {

    }

    public void findAndLoadClass(String packageName) {

    }

    public void findAndInstanceSubClass(Class<?> clazz) {
        ClassLoader classLoader = clazz.getClassLoader();

    }



}
