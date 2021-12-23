package com.github.eros.common.lang;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;

/**
 * @author fankongqiumu
 * @description TODO
 * @date 2021/12/20 15:51
 */
public class ErosFacadeLoader {

    public static final String FACADE_RESOURCE_LOCATION = "META-INF/facade.properties";

    public static <T> List<T> loadListeners(Class<T> facadeClass, ClassLoader classLoader) {
        Objects.requireNonNull(facadeClass, "'facadeClass' must not be null");
        ClassLoader classLoaderToUse = classLoader;
        if (classLoaderToUse == null) {
            classLoaderToUse = ErosFacadeLoader.class.getClassLoader();
        }
        Set<String> facadeClassNames = loadFacadeNames(facadeClass, classLoaderToUse);
        if (facadeClassNames.isEmpty()) {
            return Collections.emptyList();
        }
        List<T> result = new ArrayList<T>(facadeClassNames.size());
        for (String facadeClassName : facadeClassNames) {
            result.add(instantiateFacade(facadeClassName, facadeClass, classLoaderToUse));
        }
        return result;
    }

    private static <T> T instantiateFacade(String instanceClassName, Class<T> facadeClass, ClassLoader classLoader) {
        try {
            Class<?> instanceClass = Class.forName(instanceClassName, false, classLoader);
            if (!facadeClass.isAssignableFrom(instanceClass)) {
                throw new IllegalArgumentException(
                        "Class [" + instanceClassName + "] is not assignable to [" + facadeClass.getName() + "]");
            }
            Constructor<?> constructor = instanceClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            return (T) constructor.newInstance();
        } catch (Throwable ex) {
            throw new IllegalArgumentException("Unable to instantiate facade class: " + facadeClass.getName(), ex);
        }
    }


    public static Set<String> loadFacadeNames(Class<?> facadeClass, ClassLoader classLoader) {
        String facadeClasssName = facadeClass.getName();
        try {
            Enumeration<URL> urls = (classLoader != null ? classLoader.getResources(FACADE_RESOURCE_LOCATION) :
                    ClassLoader.getSystemResources(FACADE_RESOURCE_LOCATION));
            Set<String> result = new HashSet<>();
            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                InputStream inputStream = url.openStream();
                Properties properties = loadProperties(inputStream);
                if (properties.isEmpty()){
                    continue;
                }
                String propertyValue = properties.getProperty(facadeClasssName);
                for (String factoryName : propertyValue.split(",")) {
                    result.add(factoryName.trim());
                }
            }
            return result;
        } catch (IOException ex) {
            throw new IllegalArgumentException("Unable to load factories from location [" +
                    FACADE_RESOURCE_LOCATION + "]", ex);
        }
    }

    public static Properties loadProperties(InputStream inputStream) throws IOException {
        Properties props = new Properties();
        fillProperties(props, inputStream);
        return props;
    }


    public static void fillProperties(Properties props, InputStream inputStream) throws IOException {
        try {
            props.load(inputStream);
        } finally {
            inputStream.close();
        }
    }



}
