package com.github.eros.common.retrofit.annotation;



import java.lang.annotation.Annotation;
import java.util.Objects;

/**
 * @author fankongqiumu
 * @description
 * @date 2021/12/19 07:49
 */
public class AnnotationMetaUtils {

    public static  <AnnotationClass> AnnotationClass getAnnotations(Object object, Class<? extends Annotation> annotationClass){
        Objects.requireNonNull(object);
        Objects.requireNonNull(annotationClass);
        if (!object.getClass().isAnnotationPresent(annotationClass)) {
            return null;
        }
        Annotation annotation = object.getClass().getAnnotation(annotationClass);
        return null;
    }
}
