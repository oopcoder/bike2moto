package cn.oopcoder.b2m.bean;

import cn.oopcoder.b2m.config.StockConfig;
import cn.oopcoder.b2m.enums.ShowMode;
import cn.oopcoder.b2m.utils.JacksonUtil;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.intellij.ui.JBColor;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.StringUtils;

import java.awt.Color;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static cn.oopcoder.b2m.consts.ColorHexConst.lightRed;
import static cn.oopcoder.b2m.consts.ColorHexConst.softGreen;
import static cn.oopcoder.b2m.enums.ShowMode.Hidden;
import static cn.oopcoder.b2m.enums.ShowMode.Normal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class StockDataBean {

    public static final String STOCK_CODE_FIELD_NAME = "code";
    public static final String CHANGE_PERCENT_FIELD_NAME = "changePercent";
    public static final String CHANGE_FIELD_NAME = "change";

    // 调试用，不需要时直接注释
    // @TableColumn(name = "行号", order = 4)
    private int index;

    @Column(name = "名称", order = 6, showMode = {Normal})
    private String name;

    @Column(name = "编码", order = 8)
    private String code;

    // 隐蔽模式名，最好是英文
    @Column(name = "马赛克", order = 10, showMode = {Hidden}, hiddenModeName = "mask", editable = true)
    private String maskName;

    @Column(name = "别名", order = 15, showMode = {Normal}, editable = true)
    private String alias;

    @Column(name = "当前价", order = 20, hiddenModeName = "now", enableNumberComparator = true)
    private String currentPrice;

    @Column(name = "涨跌", order = 25, hiddenModeName = "up", enableNumberComparator = true,
            foreground = {lightRed, softGreen}, hiddenModeForeground = {lightRed, softGreen})
    private String change;

    @Column(name = "涨跌幅", order = 30, hiddenModeName = "upp", enableNumberComparator = true,
            foreground = {lightRed, softGreen}, hiddenModeForeground = {lightRed, softGreen})
    private String changePercent;

    @Column(name = "最高价", order = 35, enableNumberComparator = true)
    private String high;

    @Column(name = "最低价", order = 40, enableNumberComparator = true)
    private String low;

    @Column(name = "时间", order = 60, showMode = {Normal})
    private String time;

    // 固定在顶部
    // @TableColumn(name = "固定", order = 45)
    private boolean pinTop = false;


    public static List<ColumnDefinition> getTableColumnInfos(ShowMode showMode) {
        boolean isHidden = Hidden == showMode;

        return Arrays.stream(StockDataBean.class.getDeclaredFields())
                .filter(field -> {
                    if (!field.isAnnotationPresent(Column.class)) {
                        return false;
                    }
                    ShowMode[] showModes = field.getAnnotation(Column.class).showMode();
                    return Arrays.stream(showModes).anyMatch(sm -> sm == showMode);
                })
                .map(field -> {
                    Column tc = field.getAnnotation(Column.class);
                    String displayName = isHidden ? tc.hiddenModeName() : tc.name();
                    if (StringUtils.isEmpty(displayName)) {
                        displayName = field.getName();
                    }

                    String[] foreground = isHidden ? tc.hiddenModeForeground() : tc.foreground();
                    List<Color> displayColor = new ArrayList<>();
                    if (foreground != null && foreground.length > 0) {
                        for (String color : foreground) {
                            if (StringUtils.isEmpty(color)) {
                                displayColor.add(null);
                            } else {
                                displayColor.add(JBColor.decode(color));
                            }
                        }
                    }
                    return new ColumnDefinition(field.getName(), displayName, displayColor, tc.order(),
                            tc.enableNumberComparator(), tc.editable(), isHidden ? 60 : 100);
                })
                .sorted(Comparator.comparingInt(ColumnDefinition::getOrder))
                .collect(Collectors.toList());
    }

    public StockDataBean(StockConfig stockConfig, int index) {
        this.code = stockConfig.getCode();
        this.maskName = stockConfig.getMaskName();
        this.alias = stockConfig.getAlias();
        this.pinTop = stockConfig.isPinTop();
        this.index = index;
    }

    public Object getFieldValue(String fieldName) {
        // 使用反射获取属性值
        try {
            java.lang.reflect.Field field = StockDataBean.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(this);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void setFieldValue(String fieldName, Object value) {
        try {
            // 1. 获取字段对象
            Field field = StockDataBean.class.getDeclaredField(fieldName);
            field.setAccessible(true);

            // 2. 根据字段类型转换值
            Object convertedValue = convertValueToFieldType(value, field.getType());

            // 3. 设置字段值
            if (convertedValue != null) {
                field.set(this, convertedValue);
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
    private Object convertValueToFieldType(Object value, Class<?> targetType) {
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

    @Override
    public String toString() {
        return JacksonUtil.toJson(this);
    }
}