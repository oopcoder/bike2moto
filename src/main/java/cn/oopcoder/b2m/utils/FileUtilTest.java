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
public class FileUtilTest {

    public static InputStream getInputStreamFromFile(String fileName) {
        return FileUtilTest.class.getResourceAsStream(fileName);
    }

    public static <T> T fromJsonFile(String fileName, Class<T> clazz) {
        try (InputStream inputStream = FileUtilTest.class.getClassLoader().getResourceAsStream(fileName)) {
            if (inputStream == null) {
                throw new RuntimeException("json文件不存在");
            }
            String json = new String(inputStream.readAllBytes());
            return JacksonUtil.fromJson(json, clazz);
        } catch (IOException e) {
            log.error("反序列化json异常: {}, {}", fileName, e.getLocalizedMessage(), e);
        }
        throw new RuntimeException("获取json异常");

    }

    public static <T> T fromJsonFile(String fileName, TypeReference<T> typeReference) {
        try (InputStream inputStream = FileUtilTest.class.getClassLoader().getResourceAsStream(fileName)) {
            if (inputStream == null) {
                throw new RuntimeException("json文件不存在");
            }
            String json = new String(inputStream.readAllBytes());
            return JacksonUtil.fromJson(json, typeReference);
        } catch (IOException e) {
            log.error("反序列化json异常: {}, {}", fileName, e.getLocalizedMessage(), e);
        }
        throw new RuntimeException("获取json异常");

    }

    public static List<String> readLines(String testResource) throws IOException {
        if (!testResource.startsWith("/")) {
            testResource = "/" + testResource;
        }
        InputStream inputStream = FileUtilTest.class.getResourceAsStream(testResource);
        if (inputStream == null) {
            throw new RuntimeException("文件不存在: " + testResource);
        }
        return CharStreams.readLines(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
    }

}
