package cn.oopcoder.b2m.utils;

import cn.oopcoder.b2m.bean.StockDataBean;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Date;

public class ReflectUtil {
    public static void copy(Object src, Object desc) {
        try {
            Field[] fields = src.getClass().getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                Object value = field.get(src);
                setFieldValue(field.getName(), desc, value);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Object getFieldValue(String fieldName, Object obj) {
        // 使用反射获取属性值
        try {
            java.lang.reflect.Field field = StockDataBean.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(obj);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void setFieldValue(String fieldName, Object obj, Object value) {
        try {
            // 1. 获取字段对象
            Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);

            // 2. 根据字段类型转换值
            Object convertedValue = convertValueToFieldType(value, field.getType());

            // 3. 设置字段值
            if (convertedValue != null) {
                field.set(obj, convertedValue);
            } else {
                System.err.println("Failed to convert value for field: " + fieldName);
            }
        } catch (NoSuchFieldException e) {
            System.err.println("Field not found: " + fieldName);
        } catch (IllegalAccessException e) {
            System.err.println("Access denied to field: " + fieldName);
        }
    }

    /**
     * 将输入值转换为字段的目标类型
     */
    private static Object convertValueToFieldType(Object value, Class<?> targetType) {
        if (value == null) {
            return null;
        }

        try {
            // 处理常见类型转换
            if (targetType == String.class) {
                return value.toString();
            } else if (targetType == Integer.class || targetType == int.class) {
                return Integer.parseInt(value.toString());
            } else if (targetType == Long.class || targetType == long.class) {
                return Long.parseLong(value.toString());
            } else if (targetType == Double.class || targetType == double.class) {
                return Double.parseDouble(value.toString());
            } else if (targetType == BigDecimal.class) {
                return new BigDecimal(value.toString());
            } else if (targetType == Date.class) {
                // 假设输入是时间戳（Long）或日期字符串
                return value instanceof Long ? new Date((Long) value) : new Date(value.toString());
            } else if (targetType == Boolean.class || targetType == boolean.class) {
                return Boolean.parseBoolean(value.toString());
            }
            // 其他类型可继续扩展...
        } catch (Exception e) {
            System.err.println("Type conversion failed. Target: " + targetType + ", Input: " + value);
        }
        return null;
    }


}
