package com.github.eros.common.util;

import com.github.eros.common.exception.ErosError;
import com.github.eros.common.exception.ErosException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.util.*;

/**
 * @author fankongqiumu
 * @description
 * @date 2021/12/21 20:05
 */
public class JsonUtils {

    private static final Set<String> BLANK_JSON;

    static {
        BLANK_JSON = new HashSet<>();
        BLANK_JSON.add("[]");
        BLANK_JSON.add("{}");
    }

    private JsonUtils(){
        throw new ErosException(ErosError.SYSTEM_ERROR, "not support constructor instance");
    }

    private static final Gson GSON = new GsonBuilder().create();

    public static <T> String toJsonString(T t) {
        return GSON.toJson(t);
    }

    public static <T> T  parse(String jsonString, Class<T> classOft) {
        return GSON.fromJson(jsonString, classOft);
    }

    public static <T> List<T>  parseList(String jsonString, Class<T> classOft) {
        return GSON.fromJson(jsonString, new TypeToken<List<T>>(){}.getType());
    }

    public static <K,V> Map<K,V> parseMap(String jsonString, Class<K> classOfK,  Class<V> classOfV) {
        return GSON.fromJson(jsonString, new TypeToken<HashMap<K,V>>(){}.getType());
    }

    public static boolean isBlank(String jsonString){
        return BLANK_JSON.contains(jsonString);
    }
}
