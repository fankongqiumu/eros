package com.github.eros.starter.selector;

import com.github.eros.client.ErosClientListener;
import com.github.eros.client.step.ClientStartupStep;
import com.github.eros.starter.annotation.ErosListener;
import com.github.eros.starter.annotation.ErosStartupStep;
import com.github.eros.starter.runner.ErosRunner;
import com.github.eros.starter.scanner.ClassScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public class ErosImportSelector implements ImportSelector, ResourceLoaderAware {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private ClassScanner scanner;

    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        String baseClassName = importingClassMetadata.getClassName();
        final String packageName = ClassUtils.getPackageName(baseClassName);
        if(logger.isDebugEnabled()) {
            logger.debug("basePackage is [{}]", packageName);
        }
        // 扫描 [ErosListener + ErosStartupStep]
        Set<String> classNames = scanner.loadClass(packageName,
                annotations -> annotations.contains(ErosListener.class.getName())
                    || annotations.contains(ErosStartupStep.class.getName())
        );
        int size = classNames.size();

        List<String> targetClassNames = new ArrayList<>(size);
        for (String className : classNames) {
            try {
                Class<?> aClass = ClassUtils.forName(className, scanner.getResourceLoader().getClassLoader());
                // 必须继承相应的父类
                if (ErosClientListener.class.isAssignableFrom(aClass)
                        || ClientStartupStep.class.isAssignableFrom(aClass)){
                    targetClassNames.add(className);
                }
            } catch (ClassNotFoundException e) {
                logger.error("load class [{}] error:[{}]", className, e);
            }
        }
        // 添加 [ErosRunner] 用于启动client
        targetClassNames.add(ErosRunner.class.getName());
        return targetClassNames.toArray(new String[0]);
    }

    @Override
    public Predicate<String> getExclusionFilter() {
        return null;
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.scanner = new ClassScanner(resourceLoader);
    }
}
