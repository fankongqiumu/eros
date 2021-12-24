package com.github.eros.client.forest;

import com.dtflys.forest.converter.ForestConverter;
import com.dtflys.forest.utils.ForestDataType;
import com.github.eros.common.model.Result;
import com.github.eros.domain.Config;

import java.lang.reflect.Type;
import java.nio.charset.Charset;

public class ErosJsonForestConverter implements ForestConverter<Result<Config>> {

    /**
     * 将源数据转换为目标类型（Class）的java对象
     *
     * @param source     源数据
     * @param targetType 目标类型 (Class对象)
     * @return 转换后的目标类型对象
     */
    @Override
    public <T> T convertToJavaObject(Result<Config> source, Class<T> targetType) {
        return null;
    }

    /**
     * 将源数据转换为目标类型（Type）的java对象
     *
     * @param source     源数据
     * @param targetType 目标类型 (Type对象)
     * @return 转换后的目标类型对象
     */
    @Override
    public <T> T convertToJavaObject(Result<Config> source, Type targetType) {
        return null;
    }

    /**
     * 将源数据转换为目标类型（Class）的java对象
     *
     * @param source     源数据
     * @param targetType 目标类型 (Class对象)
     * @param charset
     * @return 转换后的目标类型对象
     */
    @Override
    public <T> T convertToJavaObject(byte[] source, Class<T> targetType, Charset charset) {
        return null;
    }

    /**
     * 将源数据转换为目标类型（Type）的java对象
     *
     * @param source     源数据
     * @param targetType 目标类型 (Type对象)
     * @param charset
     * @return 转换后的目标类型对象
     */
    @Override
    public <T> T convertToJavaObject(byte[] source, Type targetType, Charset charset) {
        return null;
    }

    /**
     * 获取当前数据转换器转换类型
     *
     * @return
     */
    @Override
    public ForestDataType getDataType() {
        return null;
    }
}
