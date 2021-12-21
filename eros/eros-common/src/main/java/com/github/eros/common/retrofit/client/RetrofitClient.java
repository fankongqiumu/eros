package com.github.eros.common.retrofit.client;

import com.github.eros.common.exception.ErosError;
import com.github.eros.common.retrofit.annotation.RetrofitClientContext;
import com.github.eros.common.retrofit.exception.ErosRetrofitException;
import com.github.eros.common.retrofit.annotation.EnableRetrofitService;
import com.github.eros.common.retrofit.util.RetrofitClientUtil;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import org.apache.commons.lang3.ArrayUtils;
import retrofit2.Call;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;


/**
 * @author fankongqiumu
 * @description
 * @date 2021/12/19 03:56
 */
public class RetrofitClient {

    /**
     * 禁止使用反射等方式设置本类实例字段，这将可能导致初始化产生问题
     */
    private OkHttpClient okHttpClient;
    
    
    /**
     * 禁止使用反射等方式实例化
     * @param retrofitServiceClass
     */
    private RetrofitClient(final Class<?> retrofitServiceClass){
        this.initInstance(retrofitServiceClass);
    }

    private static final Object lock = new Object();

    private static final Map<String, Object> RETROFIT_CLIENT_INSTANCE_GROUP_HOLDER = new ConcurrentHashMap<>(256);

    private static final Map<Class<?>, Object> RETROFIT_SERVICE_INSTANCE_HOLDER = new ConcurrentHashMap<>(256);

    private static final String CALL_METHOD_DEFINE_TEMPLATE = "\n@EnableRetrofitService\n" +
            "public interface DemoService {\n" +
            "    \n" +
            "    @GET(\"/test/demo\")\n" +
            "    Call<Result<String>> demo();\n" +
            "    \n" +
            "}";

    public static class Builder {
        public static RetrofitClient build(final Class<?> retrofitServiceClass) {
            retrofitServiceValidate(retrofitServiceClass);
            EnableRetrofitService enableRetrofitService = getEnableRetrofitService(retrofitServiceClass);
            String group = enableRetrofitService.group();
            Object retrofitClientInstance = RETROFIT_CLIENT_INSTANCE_GROUP_HOLDER.get(group);
            if (null != retrofitClientInstance) {
                return (RetrofitClient) retrofitClientInstance;
            }
            synchronized (lock) {
                if (null == RETROFIT_CLIENT_INSTANCE_GROUP_HOLDER.get(group)) {
                    return new RetrofitClient(retrofitServiceClass);
                }
            }
            return (RetrofitClient) RETROFIT_CLIENT_INSTANCE_GROUP_HOLDER.get(group);
        }
    }

    private void initInstance(final Class<?> retrofitServiceClass){
        EnableRetrofitService enableRetrofitService = getEnableRetrofitService(retrofitServiceClass);
        RetrofitClientContext context  = getRetrofitClientContext(enableRetrofitService);
        retrofitServiceRequestInfoValidate(context.getServiceRequestAttribute());
        synchronized (lock) {
            final ExecuteThreadPoolConfig executeThreadPoolConfig = getExecuteThreadPoolConfig(context);
            final Dispatcher dispatcher = new Dispatcher(
                    new ThreadPoolExecutor(executeThreadPoolConfig.getCorePoolSize(),
                            executeThreadPoolConfig.getMaximumPoolSize(),
                            executeThreadPoolConfig.getKeepAliveTime(),
                            executeThreadPoolConfig.getUnit(),
                            executeThreadPoolConfig.getWorkQueue(),
                            executeThreadPoolConfig.getThreadFactory())
            );
            dispatcher.setMaxRequestsPerHost(10);

            final ConnectionPoolConfig connectionPoolConfig = getConnectionPoolConfig(context);
            final ServiceRequestAttribute serviceRequestAttribute = context.getServiceRequestAttribute();
            this.okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(context.getInterceptor())
                    .dispatcher(dispatcher)
                    .connectionPool(new ConnectionPool(connectionPoolConfig.getMaxIdleConnections(), connectionPoolConfig.getKeepAliveDuration(), connectionPoolConfig.getTimeUnit()))
                    .connectTimeout(serviceRequestAttribute.getConnectTimeout(), serviceRequestAttribute.getUnit())
                    .readTimeout(serviceRequestAttribute.getReadTimeout(), serviceRequestAttribute.getUnit())
                    .writeTimeout(serviceRequestAttribute.getWriteTimeout(), serviceRequestAttribute.getUnit())
                    .build();

            RETROFIT_CLIENT_INSTANCE_GROUP_HOLDER.putIfAbsent(enableRetrofitService.group(), this);
        }
    }

    public <RetrofitService> RetrofitService getRetrofitServiceInstance(final Class<RetrofitService> retrofitServiceClass){
        if (RETROFIT_SERVICE_INSTANCE_HOLDER.containsKey(retrofitServiceClass)) {
            return (RetrofitService) RETROFIT_SERVICE_INSTANCE_HOLDER.get(retrofitServiceClass);
        }
        synchronized (lock) {
            if (!RETROFIT_SERVICE_INSTANCE_HOLDER.containsKey(retrofitServiceClass)) {
                EnableRetrofitService enableRetrofitService = getEnableRetrofitService(retrofitServiceClass);
                RetrofitClient retrofitClient = (RetrofitClient) RETROFIT_CLIENT_INSTANCE_GROUP_HOLDER.get(enableRetrofitService.group());
                // todo baseUrl check
                RetrofitService retrofitService = RetrofitClientUtil.getDefaultService(retrofitClient.okHttpClient, enableRetrofitService.baseUrl(), retrofitServiceClass);
                RETROFIT_SERVICE_INSTANCE_HOLDER.putIfAbsent(retrofitServiceClass, retrofitService);
            }
        }
        return (RetrofitService) RETROFIT_SERVICE_INSTANCE_HOLDER.get(retrofitServiceClass);
    }

    public static EnableRetrofitService getEnableRetrofitService(final Class<?> retrofitServiceClass) {
        EnableRetrofitService enableRetrofitService = retrofitServiceClass.getAnnotation(EnableRetrofitService.class);
        if (enableRetrofitService == null) {
            throw new ErosRetrofitException(ErosError.SYSTEM_ERROR, "getRetrofitClientContext error, retrofitServiceClass not be @EnableRetrofitService annotated");
        }
        return enableRetrofitService;
    }

    public static RetrofitClientContext getRetrofitClientContext(final EnableRetrofitService enableRetrofitService) {
        Class<?> contextClass = enableRetrofitService.context();
        enableRetrofitServiceContexClasstValidate(contextClass);
        try {
            Constructor<?> constructor = contextClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            Object instance = constructor.newInstance();
            return (RetrofitClientContext) instance;
        } catch (Exception e) {
            throw new ErosRetrofitException(ErosError.SYSTEM_ERROR, "getRetrofitClientContext error...", e);
        }
    }

    public static ConnectionPoolConfig getConnectionPoolConfig(final RetrofitClientContext context) {
        if (context == null){
            throw new ErosRetrofitException(ErosError.SYSTEM_ERROR, "RetrofitClientContext can not be null...");
        }
        ConnectionPoolConfig connectionPoolConfig = context.getConnectionPoolConfig();
        if (null == connectionPoolConfig) {
            throw new ErosRetrofitException(ErosError.SYSTEM_ERROR, "RetrofitClientContext-connectionPoolConfig can not be null...");
        }
        return connectionPoolConfig;
    }

    public static ExecuteThreadPoolConfig getExecuteThreadPoolConfig(final RetrofitClientContext context){
        if (context == null){
            throw new ErosRetrofitException(ErosError.SYSTEM_ERROR, "RetrofitClientContext can not be null...");
        }
        ExecuteThreadPoolConfig executeThreadPoolConfig = context.getExecuteThreadPoolConfig();
        if (null == executeThreadPoolConfig) {
            throw new ErosRetrofitException(ErosError.SYSTEM_ERROR, "RetrofitClientContext-executeThreadPoolConfig can not be null...");
        }
        return executeThreadPoolConfig;
    }

    public static void retrofitServiceRequestInfoValidate(ServiceRequestAttribute serviceRequestAttribute){
        if (serviceRequestAttribute == null){
            throw new ErosRetrofitException(ErosError.SYSTEM_ERROR, "serviceRequestInfo can not be null...");
        }
        // todo something
    }

    private static void enableRetrofitServiceContexClasstValidate(Class<?> retrofitServiceContextClass){
        Class<?>[] interfaces = retrofitServiceContextClass.getInterfaces();
        if (ArrayUtils.isEmpty(interfaces)) {
            throw new ErosRetrofitException(ErosError.SYSTEM_ERROR, "retrofitServiceContextClass must impl RetrofitClientContext.class...");
        }
        for (Class<?> anInterface : interfaces) {
            if (anInterface == RetrofitClientContext.class){
                return;
            }
        }
        throw new ErosRetrofitException(ErosError.SYSTEM_ERROR, "retrofitServiceContextClass must impl RetrofitClientContext.class...");
    }
    
    private static <RetrofitService> void retrofitServiceValidate(Class<RetrofitService> retrofitServiceClass){
        if (retrofitServiceClass == null){
            throw new ErosRetrofitException(ErosError.SYSTEM_ERROR, "retrofitServiceClass can not be null...");
        }

        boolean isInterface = retrofitServiceClass.isInterface();
        boolean annotationByEnableRetrofitService = retrofitServiceClass.isAnnotationPresent(EnableRetrofitService.class);
        if (!isInterface || !annotationByEnableRetrofitService) {
            throw new ErosRetrofitException(ErosError.SYSTEM_ERROR, "the retrofit service must be an interface and must be @EnableRetrofitService annotated, example: " + CALL_METHOD_DEFINE_TEMPLATE);
        }

        Method[] methods = retrofitServiceClass.getDeclaredMethods();
        if (ArrayUtils.isEmpty(methods)){
            throw new ErosRetrofitException(ErosError.SYSTEM_ERROR, "the retrofit service no declared method");
        }
        for (Method method : methods) {
            Class<?> returnType = method.getReturnType();
            boolean returnTypeAssignableFromCallInterface = returnType.isAssignableFrom(Call.class);
            if (!returnTypeAssignableFromCallInterface) {
                throw new ErosRetrofitException(ErosError.SYSTEM_ERROR, "the method [" + method.toGenericString() +"] return type must be: retrofit2.Call, example: " + CALL_METHOD_DEFINE_TEMPLATE);
            }
        }
    }

}
