package com.github.eros.starter.scanner;

import com.github.eros.common.constant.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

/**
 * @author fankongqiumu
 * @Description 加载指定包下的类
 */
public class ClassScanner {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String DEFAULT_SUFFIX = "/**/*.class";
    private static final String DEFAULT_CLASS_PATH = "classpath*:";

    private final ResourceLoader resourceLoader;

    public ResourceLoader getResourceLoader() {
        return resourceLoader;
    }

    public ClassScanner(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    /**
     * packageName 下所有的类都会被加载
     * @param packageName
     */
    public Set<String> loadClass(String packageName) {
        return loadClass(packageName, null);
    }

    /**
     * packageName 下所有满足 #predicate.test=true 的类都会被加载
     * @param packageName
     * @param predicate
     */
    public Set<String> loadClass(String packageName, Predicate<Set<String>> predicate) {
        return loadClass(packageName, predicate, false);
    }

    /**
     * 以allLoad 为主
     *  #allLoad=true 将忽略predicate
     *  #allLoad=false 以predicate.test为结果
     * @param packageName
     * @param predicate
     * @param allLoad
     */
    public Set<String> loadClass(String packageName, Predicate<Set<String>> predicate, boolean allLoad) {
        ResourcePatternResolver resolver = ResourcePatternUtils.getResourcePatternResolver(resourceLoader);
        CachingMetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(resourceLoader);
        //然后把我们的包名basPack转换为路径名
        String basePackPath = packageName.replace(".", File.separator);
        String searchPath = DEFAULT_CLASS_PATH + basePackPath + DEFAULT_SUFFIX;

        Resource[] resources = null;
        try {
            resources = resolver.getResources(searchPath);
        } catch (IOException e) {
            logger.error("resolver.getResources error:", e);
        }
        if (null == resources || resources.length <= Constants.INTEGER_ZERO){
            return Collections.emptySet();
        }
        Set<String> classes = new HashSet<>();
        for (Resource resource : resources) {
            MetadataReader reader;
            try {
                reader = metadataReaderFactory.getMetadataReader(resource);

                AnnotationMetadata annotationMetadata = reader.getAnnotationMetadata();
                Set<String> annotationTypes = annotationMetadata.getAnnotationTypes();
                String className = annotationMetadata.getClassName();

                boolean isPredicate = allLoad || (null == predicate) || predicate.test(annotationTypes);
                if (isPredicate) {
                    classes.add(className);
                    logger.info("scanner class [{}]", className);
                }
            } catch (IOException e) {
                logger.error("metadataReaderFactory.getMetadataReader error:", e);
            }
        }
        return classes;
    }

}