package cn.oopcoder.b2m.config;

import com.intellij.ide.util.PropertiesComponent;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import cn.oopcoder.b2m.bean.StockDataBean;
import cn.oopcoder.b2m.bean.TableFieldInfo;
import cn.oopcoder.b2m.utils.JacksonUtil;
import lombok.Data;

/**
 * Created by oopcoder at 2025/5/31 9:42 .
 */

public class GlobalConfig {

    private static final GlobalConfig ourInstance = new GlobalConfig();

    private static final String GLOBAL_CONFIG_KEY = "b2m.global.config";

    public static GlobalConfig getInstance() {
        return ourInstance;
    }

    private GlobalConfig() {
        propertiesComponent = PropertiesComponent.getInstance();
        refresh();
    }

    private PropertiesComponent propertiesComponent = null;

    private volatile Config config;

    public void refresh() {
        String configJson = propertiesComponent.getValue(GLOBAL_CONFIG_KEY);
        if (StringUtils.isEmpty(configJson)) {
            config = null;
            System.out.println("refresh(): 配置为空");
            return;
        }
        config = JacksonUtil.fromJson(configJson, Config.class);
        System.out.println("refresh(): 刷新配置成功");

    }

    public void persist() {
        if (config != null) {
            propertiesComponent.setValue(GLOBAL_CONFIG_KEY, JacksonUtil.toJson(this.config));
        } else {
            clear();
        }
    }

    public void clear() {
        // 清空
        propertiesComponent.unsetValue(GLOBAL_CONFIG_KEY);

        config = null;
    }

    public GlobalConfig setStockTableColumn(List<String> tableColumn) {

        if (config == null) {
            config = new Config();
        }
        config.setStockTableColumn(tableColumn);
        return this;
    }

    public String[] getStockTableColumn(boolean hidden) {
        return getStockTableFieldInfo(hidden).stream().map(TableFieldInfo::displayName).toArray(String[]::new);
    }

    public List<TableFieldInfo> getStockTableFieldInfo(boolean hidden) {
        List<TableFieldInfo> fieldInfoList = hidden ? StockDataBean.hiddenTableFields : StockDataBean.normalTableFields;

        if (config == null || config.getStockTableColumn() == null || config.getStockTableColumn().isEmpty()) {
            return fieldInfoList;
        }

        // 排序
        Map<String, TableFieldInfo> map = fieldInfoList.stream()
                .collect(Collectors.toMap(TableFieldInfo::fieldName, Function.identity()));

        List<TableFieldInfo> result = new ArrayList<>();
        for (String fieldName : config.getStockTableColumn()) {
            TableFieldInfo fieldInfo = map.get(fieldName);
            if (fieldInfo != null) {
                result.add(fieldInfo);
                map.remove(fieldName);
            }
        }
        result.addAll(map.values());
        return result;
    }

    @Data
    public static class Config {

        List<String> stockTableColumn;
    }

}
