package cn.oopcoder.b2m.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.SerializationFeature.FAIL_ON_EMPTY_BEANS;


@Slf4j
public class JacksonUtil {

    private static final ObjectMapper DEFAULT_INSTANCE = newObjectMapper();

    public static void customizeJackson(ObjectMapper mapper) {
        mapper.disable(FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.disable(FAIL_ON_EMPTY_BEANS);
        SimpleDateFormat smt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        mapper.setDateFormat(smt);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    public static ObjectMapper newObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        customizeJackson(mapper);
        return mapper;
    }

    public static <T> T fromJson(String json, TypeReference<T> typeReference) {
        if (Strings.isNullOrEmpty(json)) {
            return null;
        }
        try {
            return DEFAULT_INSTANCE.readValue(json, typeReference);
        } catch (Exception e) {
            log.error("反序列化json异常，json: {}, {}", json, e.getLocalizedMessage(), e);
            throw new RuntimeException("反序列化json异常");
        }
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        if (Strings.isNullOrEmpty(json)) {
            return null;
        }
        try {
            return DEFAULT_INSTANCE.readValue(json, clazz);
        } catch (Exception e) {
            log.error("反序列化json异常，json: {}, {}", json, e.getLocalizedMessage(), e);
            throw new RuntimeException("反序列化json异常");
        }
    }

    public static String toJson(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return DEFAULT_INSTANCE.writeValueAsString(obj);
        } catch (Exception e) {
            log.error("序列化json异常，{}, {}", obj, e.getLocalizedMessage(), e);
            throw new RuntimeException("反序列化json异常");
        }
    }

}
