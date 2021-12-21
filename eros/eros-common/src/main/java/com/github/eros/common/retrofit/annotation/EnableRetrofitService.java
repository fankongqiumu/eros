package com.github.eros.common.retrofit.annotation;

import com.github.eros.common.retrofit.client.DefaultRetrofitClientContext;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author fankongqiumu
 * @description
 * @date 2021/12/19 02:55
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface EnableRetrofitService {

    String group() default "defaultGroup";

    String baseUrl();

    /**
     * extends RetrofitClientContext
     * @return
     */
    Class<?> context() default DefaultRetrofitClientContext.class;
}
