package cn.oopcoder.b2m.bean;

import cn.oopcoder.b2m.enums.ShowMode;
import cn.oopcoder.b2m.utils.JacksonUtil;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.sisu.Hidden;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static cn.oopcoder.b2m.enums.ShowMode.Hidden;
import static cn.oopcoder.b2m.enums.ShowMode.Normal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class StockDataBean {

    // 隐蔽模式名，最好是英文
    @TableColumn(name = "标识", order = 1, showMode = {Hidden}, hiddenModeName = "mask")
    private String maskName;

    @TableColumn(name = "别名", order = 2, showMode = {Normal})
    private String alias;

    @TableColumn(name = "当前价", order = 3, hiddenModeName = "now")
    private String currentPrice;

    @TableColumn(name = "涨跌", order = 4, hiddenModeName = "up")
    private String change;

    @TableColumn(name = "涨跌幅", order = 5, hiddenModeName = "upp")
    private String changePercent;

    @TableColumn(name = "最高价", order = 6)
    private String high;

    @TableColumn(name = "最低价", order = 10)
    private String low;

    @TableColumn(name = "代码", order = 11)
    private String code;

    @TableColumn(name = "名称", order = 12, showMode = {Normal})
    private String name;

    @TableColumn(name = "时间", order = 13, showMode = {Normal})
    private String time;

    public static List<TableFieldInfo> hiddenTableFields = getTableColumns(Hidden);
    // public static String[] hiddenTableColumns = hiddenTableFields.stream().map(TableFieldInfo::displayName).toArray(String[]::new);

    public static List<TableFieldInfo> normalTableFields = getTableColumns(Normal);
    // public static String[] normalTableColumns = normalTableFields.stream().map(TableFieldInfo::displayName).toArray(String[]::new);

    public static List<TableFieldInfo> getTableColumns(ShowMode showMode) {
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
                    String displayName = Hidden == showMode ? tc.hiddenModeName() : tc.name();
                    if (StringUtils.isEmpty(displayName)) {
                        displayName = f.getName();
                    }
                    return new TableFieldInfo(f.getName(), displayName, tc.order());
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

    @Override
    public String toString() {
        return JacksonUtil.toJson(this);
    }
}