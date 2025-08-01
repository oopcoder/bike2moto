package cn.oopcoder.b2m.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.io.CharStreams;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
public class FileUtil {

    public static InputStream getInputStreamFromFile(String fileName) {
        return FileUtil.class.getResourceAsStream(fileName);
    }

    public static <T> T fromJsonFile(String fileName, Class<T> clazz) {
        String json = readString(fileName);
        return JacksonUtil.fromJson(json, clazz);

    }

    public static <T> T fromJsonFile(String fileName, TypeReference<T> typeReference) {
        String json = readString(fileName);
        return JacksonUtil.fromJson(json, typeReference);
    }

    public static String readString(String fileName) {
        InputStream inputStream = FileUtil.class.getClassLoader().getResourceAsStream(fileName);
        if (inputStream == null) {
            throw new RuntimeException("文件不存在");
        }
        try {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("读取文件异常");
        }

    }

    public static List<String> readLines(String testResource) throws IOException {
        if (!testResource.startsWith("/")) {
            testResource = "/" + testResource;
        }
        InputStream inputStream = FileUtil.class.getResourceAsStream(testResource);
        if (inputStream == null) {
            throw new RuntimeException("文件不存在: " + testResource);
        }
        return CharStreams.readLines(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
    }

}
