package cn.oopcoder.b2m.utils;

import org.apache.commons.lang3.ObjectUtils;

import java.math.BigDecimal;

public class NumUtil {

    public static Double toDouble(Object object) {
        return toDouble(object, 0.0);
    }

    public static Double toDouble(Object object, Double defaultValue) {
        if (ObjectUtils.isEmpty(object)) {
            return defaultValue;
        }

        if (object instanceof Double) {
            return (Double) object;
        }

        try {
            return Double.parseDouble(object.toString());
        } catch (NumberFormatException var4) {
            var4.printStackTrace();
            return defaultValue;
        }
    }


    /**
     * 格式化小数位数的函数，确保小数位数在最小值和最大值之间，并去掉末尾多余的0
     *
     * @param value             要格式化的原始数值
     * @param minFractionDigits 最少保留的小数位数
     * @param maxFractionDigits 最多保留的小数位数
     * @return 格式化后的字符串表示，小数位数在[min,max]范围且去掉末尾多余的0
     */
    public static String formatDecimal(double value, int minFractionDigits, int maxFractionDigits) {
        // 参数验证
        if (minFractionDigits < 0 || maxFractionDigits < 0 || minFractionDigits > maxFractionDigits) {
            throw new IllegalArgumentException("Invalid fraction digits range");
        }

        // 使用BigDecimal.valueOf避免精度问题
        BigDecimal bd = BigDecimal.valueOf(value);
        // 使用BigDecimal进行精确的四舍五入处理
        bd = bd.setScale(maxFractionDigits, BigDecimal.ROUND_HALF_UP);
        StringBuilder formatted = new StringBuilder(bd.toPlainString());

        // 到这里小数位数都是 maxFractionDigits

        // 去掉末尾多余的0
        if (formatted.toString().indexOf('.') > 0) {
            formatted = new StringBuilder(formatted.toString().replaceAll("0+$", "").replaceAll("\\.$", ""));
        }

        // 补充必要的小数位数以满足最小位数要求
        int dotIndex = formatted.toString().indexOf('.');
        if (dotIndex >= 0) {
            int currentFractionDigits = formatted.length() - dotIndex - 1;
            if (currentFractionDigits < minFractionDigits) {
                String formatPattern = "%." + minFractionDigits + "f";
                formatted = new StringBuilder(String.format(formatPattern, Double.parseDouble(formatted.toString())));
            }
            return formatted.toString();
        }

        if (minFractionDigits > 0) {
            formatted.append(".").append("0".repeat(minFractionDigits));
        }
        return formatted.toString();
    }

}
