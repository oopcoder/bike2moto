package cn.oopcoder.b2m.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.intellij.ide.util.PropertiesComponent;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import cn.oopcoder.b2m.bean.StockDataBean;
import cn.oopcoder.b2m.bean.TableFieldInfo;
import cn.oopcoder.b2m.enums.ShowMode;
import cn.oopcoder.b2m.utils.FileUtilTest;
import cn.oopcoder.b2m.utils.JacksonUtil;
import lombok.Data;

import static cn.oopcoder.b2m.enums.ShowMode.Hidden;

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

    private ShowMode showMode = Hidden;

    public void setShowMode(ShowMode showMode) {
        this.showMode = showMode;
    }

    public boolean isHiddenMode() {
        return showMode == Hidden;
    }

    public Config getConfig() {
        return config;
    }

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

    /**
     * @param tableColumn 隐藏模式 是隐藏名，正常模式 是正常中文名
     */
    public GlobalConfig setStockTableColumn(List<String> tableColumn) {
        if (config == null) {
            config = new Config();
        }

        Map<String, TableFieldInfo> fieldInfoMap = getStockDisplayNameMap();

        // 要转成 真正的字段名
        List<String> fieldNameList = new ArrayList<>();

        for (String fieldName : tableColumn) {
            TableFieldInfo fieldInfo = fieldInfoMap.get(fieldName);
            fieldNameList.add(fieldInfo.fieldName());
        }
        config.setStockTableColumn(fieldNameList);
        return this;
    }

    public GlobalConfig setStockConfig(String stockConfig) {
        if (config == null) {
            config = new Config();
        }
        config.setStockConfig(stockConfig);
        return this;
    }

    private Map<String, StockDataBean> getDefaultStockDataBeanMap() {
        List<StockDataBean> stockDataBeanList = FileUtilTest.fromJsonFile("config/DefaultStockConfig.json", new TypeReference<>() {
        });
        return stockDataBeanList.stream()
                .collect(Collectors.toMap(StockDataBean::getCode, Function.identity()));
    }

    public Map<String, StockDataBean> getStockDataBeanMap() {
        if (config == null || StringUtils.isEmpty(config.getStockConfig())) {
            return getDefaultStockDataBeanMap();
        }
        List<StockDataBean> stockDataBeanList = JacksonUtil.fromJson(config.getStockConfig(), new TypeReference<>() {
        });
        if (stockDataBeanList == null) {
            return getDefaultStockDataBeanMap();
        }
        return stockDataBeanList.stream()
                .collect(Collectors.toMap(StockDataBean::getCode, Function.identity()));
    }

    private List<TableFieldInfo> getDefaultStockTableFieldInfo() {
        return isHiddenMode() ? StockDataBean.hiddenTableFields : StockDataBean.normalTableFields;
    }

    public Map<String, TableFieldInfo> getStockDisplayNameMap() {
        List<TableFieldInfo> fieldInfoList = getDefaultStockTableFieldInfo();
        return fieldInfoList.stream()
                .collect(Collectors.toMap(TableFieldInfo::displayName, Function.identity()));
    }

    /**
     * 按配置文件排序，因为列的顺序可以变更过
     */
    public List<TableFieldInfo> getStockTableFieldInfoOrder() {
        List<TableFieldInfo> fieldInfoList = getDefaultStockTableFieldInfo();

        if (config == null || config.getStockTableColumn() == null || config.getStockTableColumn().isEmpty()) {
            return fieldInfoList;
        }

        Map<String, TableFieldInfo> map = fieldInfoList.stream()
                .collect(Collectors.toMap(TableFieldInfo::fieldName, Function.identity()));

        // 按配置文件排序，因为列的顺序可以变更过
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

        String stockConfig;
    }

}
