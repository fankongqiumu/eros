package com.github.eros.common.lang;

import com.github.eros.common.exception.ErosError;
import com.github.eros.common.exception.ErosException;
import org.apache.commons.lang3.StringUtils;

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
 * @description
 * @date 2021/12/20 15:51
 */
public class FacadeLoader {

    public static final String FACADE_RESOURCE_LOCATION = "META-INF/eros.facade";

    public static <T> Set<String> loadListeners(Class<T> facadeClass, ClassLoader classLoader) {
        Objects.requireNonNull(facadeClass, "'facadeClass' must not be null");
        ClassLoader classLoaderToUse = classLoader;
        if (classLoaderToUse == null) {
            classLoaderToUse = FacadeLoader.class.getClassLoader();
        }
        Set<String> facadeClassNames = loadFacadeNames(facadeClass, classLoaderToUse);
        if (facadeClassNames.isEmpty()) {
            return Collections.emptySet();
        }
        return facadeClassNames;
    }

    public static <T> T instantiateFacade(String instanceClassName, Class<T> facadeClass, ClassLoader classLoader) {
        try {
            Class<?> instanceClass = Class.forName(instanceClassName, false, classLoader);
            if (!facadeClass.isAssignableFrom(instanceClass)) {
                throw new ErosException(ErosError.SYSTEM_ERROR,
                        "Class [" + instanceClassName + "] is not assignable to [" + facadeClass.getName() + "]");
            }
            Constructor<?> constructor = instanceClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            return (T) constructor.newInstance();
        } catch (Throwable e) {
            throw new ErosException(ErosError.SYSTEM_ERROR,
                    "Unable to instantiate facade class: " + facadeClass.getName(), e);
        }
    }


    public static Set<String> loadFacadeNames(Class<?> facadeClass, ClassLoader classLoader) {
        String facadeClasssName = facadeClass.getName();
        try {
            Enumeration<URL> urls = (classLoader != null
                    ? classLoader.getResources(FACADE_RESOURCE_LOCATION)
                    : ClassLoader.getSystemResources(FACADE_RESOURCE_LOCATION));
            Set<String> result = new HashSet<>();
            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                Properties properties = loadProperties(url);
                if (properties.isEmpty()){
                    continue;
                }
                String propertyValue = properties.getProperty(facadeClasssName);
                if (StringUtils.isBlank(propertyValue)){
                    continue;
                }
                for (String factoryName : propertyValue.split(",")) {
                    result.add(factoryName.trim());
                }
            }
            return result;
        } catch (IOException e) {
            throw new ErosException(ErosError.SYSTEM_ERROR,
                    "Unable to load factories from location [" + FACADE_RESOURCE_LOCATION + "]", e);
        }
    }

    public static Properties loadProperties(URL url) throws IOException {
        Properties props = new Properties();
        fillProperties(props, url);
        return props;
    }


    public static void fillProperties(Properties props, URL url) throws IOException {
        try(InputStream inputStream = url.openStream()){
            props.load(inputStream);
        } catch (Throwable throwable) {
            throw new ErosException(ErosError.SYSTEM_ERROR, throwable);
        }
    }
}
