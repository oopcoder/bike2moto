package cn.oopcoder.b2m.bean;

import cn.oopcoder.b2m.enums.ShowMode;
import cn.oopcoder.b2m.utils.JacksonUtil;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.intellij.ui.JBColor;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.sisu.Hidden;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
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

    // 隐蔽模式名，最好是英文
    @TableColumn(name = "马赛克", order = 10, showMode = {Hidden}, hiddenModeName = "mask", editable = true)
    private String maskName;

    @TableColumn(name = "别名", order = 15, showMode = {Normal}, editable = true)
    private String alias;

    @TableColumn(name = "当前价", order = 20, hiddenModeName = "now", enableNumberComparator = true)
    private String currentPrice;

    @TableColumn(name = "涨跌", order = 25, hiddenModeName = "up", enableNumberComparator = true,
            foreground = {lightRed, softGreen}, hiddenModeForeground = {lightRed, softGreen})
    private String change;

    @TableColumn(name = "涨跌幅", order = 30, hiddenModeName = "upp", enableNumberComparator = true,
            foreground = {lightRed, softGreen}, hiddenModeForeground = {lightRed, softGreen})
    private String changePercent;

    @TableColumn(name = "最高价", order = 35, enableNumberComparator = true)
    private String high;

    @TableColumn(name = "最低价", order = 40, enableNumberComparator = true)
    private String low;

    @TableColumn(name = "代码", order = 45, editable = true)
    private String code;

    @TableColumn(name = "排序", order = 50, editable = true)
    private int index = 0;

    @TableColumn(name = "名称", order = 55, showMode = {Normal})
    private String name;

    @TableColumn(name = "时间", order = 60, showMode = {Normal})
    private String time;

    public static List<TableFieldInfo> hiddenTableFields = getTableColumns(Hidden);
    public static List<TableFieldInfo> normalTableFields = getTableColumns(Normal);

    public static List<TableFieldInfo> getTableColumns(ShowMode showMode) {
        boolean isHidden = Hidden == showMode;

        return Arrays.stream(StockDataBean.class.getDeclaredFields())
                .filter(f -> {
                    if (!f.isAnnotationPresent(TableColumn.class)) {
                        return false;
                    }
                    ShowMode[] showModes = f.getAnnotation(TableColumn.class).showMode();
                    return Arrays.stream(showModes).anyMatch(sm -> sm == showMode);
                })
                .map(f -> {
                    TableColumn tc = f.getAnnotation(TableColumn.class);
                    String displayName = isHidden ? tc.hiddenModeName() : tc.name();
                    if (StringUtils.isEmpty(displayName)) {
                        displayName = f.getName();
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
                    return new TableFieldInfo(f.getName(), displayName, displayColor, tc.order(),
                            tc.enableNumberComparator(), tc.editable());
                })
                .sorted(Comparator.comparingInt(TableFieldInfo::order))
                .collect(Collectors.toList());
    }

    public StockDataBean(String code) {
        this.code = code;
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

    public void setFieldValue(String fieldName, String value) {
        // 使用反射获取属性值
        try {
            java.lang.reflect.Field field = StockDataBean.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(this, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

    }

    @Override
    public String toString() {
        return JacksonUtil.toJson(this);
    }
}