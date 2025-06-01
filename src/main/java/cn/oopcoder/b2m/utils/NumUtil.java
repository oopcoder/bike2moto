package cn.oopcoder.b2m.utils;

public class NumUtil {

    public static double toDouble(String str) {
        return toDouble(str, 0.0);
    }

    public static double toDouble(String str, double defaultValue) {
        if (str == null) {
            return defaultValue;
        } else {
            try {
                return Double.parseDouble(str);
            } catch (NumberFormatException var4) {
                return defaultValue;
            }
        }
    }
}
