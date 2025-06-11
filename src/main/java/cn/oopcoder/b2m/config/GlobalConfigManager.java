package cn.oopcoder.b2m.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.intellij.ide.util.PropertiesComponent;

import lombok.Setter;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import cn.oopcoder.b2m.bean.StockDataBean;
import cn.oopcoder.b2m.bean.TableColumnInfo;
import cn.oopcoder.b2m.enums.ShowMode;
import cn.oopcoder.b2m.utils.FileUtil;
import cn.oopcoder.b2m.utils.JacksonUtil;

import static cn.oopcoder.b2m.enums.ShowMode.Hidden;

/**
 * Created by oopcoder at 2025/5/31 9:42 .
 */

public class GlobalConfigManager {


    private static final GlobalConfigManager ourInstance = new GlobalConfigManager();

    private static final String GLOBAL_CONFIG_KEY = "b2m.global.config";

    public static GlobalConfigManager getInstance() {
        return ourInstance;
    }

    private GlobalConfigManager() {
        // 使用PropertiesComponent.getInstance()存储应用程序级值
        // 使用PropertiesComponent.getInstance(Project)方法存储项目级值
        // !!! 注意非正常关闭可能导致配置数据丢失，没有刷到硬盘中，调试重启、停止属于非正常关闭

        // 正式版本的存储地址               C:\Users\xxxx\AppData\Roaming\JetBrains\IntelliJIdea2025.1\options\other.xml
        // 开发沙箱环境idea的存储地址        build/idea-sandbox/IC-2024.2.5/config/options/other.xml
        propertiesComponent = PropertiesComponent.getInstance();
        refresh();
    }

    private PropertiesComponent propertiesComponent = null;

    private volatile GlobalConfig config;

    @Setter
    private ShowMode showMode = Hidden;

    public boolean isHiddenMode() {
        return showMode == Hidden;
    }

    public void refresh() {
        String configJson = propertiesComponent.getValue(GLOBAL_CONFIG_KEY);
        System.out.println("加载配置成功：【" + configJson + " 】");

        if (StringUtils.isEmpty(configJson)) {
            config = null;
            System.out.println("refresh(): 配置为空");
            return;
        }
        config = JacksonUtil.fromJson(configJson, GlobalConfig.class);
        System.out.println("refresh(): 刷新配置成功");
    }

    private void persist() {
        if (config != null) {
            String json = JacksonUtil.toJson(this.config);
            propertiesComponent.setValue(GLOBAL_CONFIG_KEY, json);
            System.out.println("配置持久化成功：【" + json + " 】");
        } else {
            System.out.println("persist() 持久化失败，配置为空！！！！！ ");
        }
    }

    public void clear() {
        String json = config == null ? null : JacksonUtil.toJson(this.config);
        // propertiesComponent.unsetValue(GLOBAL_CONFIG_KEY);// 这个没效果？有空测试一下
        propertiesComponent.setValue(GLOBAL_CONFIG_KEY, "");
        System.out.println("配置清除成功：【" + json + " 】");
        config = null;

    }

    /**
     * @param displayNames 隐藏模式 是隐藏名，正常模式 是正常中文名
     */
    public void persistStockTableColumn(List<String> displayNames) {
        if (config == null) {
            config = new GlobalConfig();
        }

        List<TableColumnInfo> fieldInfoList = getDefaultTableColumnInfo();

        Map<String, TableColumnInfo> fieldInfoMap = fieldInfoList.stream()
                .collect(Collectors.toMap(TableColumnInfo::getDisplayName, Function.identity()));

        // 要转成 真正的字段名
        List<String> fieldNameList = new ArrayList<>();

        for (String displayName : displayNames) {
            TableColumnInfo fieldInfo = fieldInfoMap.get(displayName);
            fieldNameList.add(fieldInfo.getFieldName());
        }
        config.setStockTableColumn(fieldNameList);
        persist();
    }

    public void persistStockConfig(List<StockConfig> stockConfig) {
        if (config == null) {
            config = new GlobalConfig();
        }
        config.setStockConfig(stockConfig);
        persist();
    }

    public List<StockConfig> getStockConfig() {
        if (config == null || config.getStockConfig() == null || config.getStockConfig().isEmpty()) {
            String json = FileUtil.readString("config/DefaultStockConfig.json");
            List<StockConfig> stockDataBeanList = JacksonUtil.fromJson(json, new TypeReference<>() {
            });
            assert stockDataBeanList != null;
            List<StockConfig> uniqueList = stockDataBeanList.stream()
                    .distinct()
                    .collect(Collectors.toList());
            persistStockConfig(uniqueList);
            return uniqueList;
        }
        return config.getStockConfig();
    }

    private List<TableColumnInfo> getDefaultTableColumnInfo() {
        return isHiddenMode() ? StockDataBean.hiddenTableColumnInfos : StockDataBean.normalTableColumnInfos;
    }

    /**
     * 按配置文件排序，因为列的顺序可以变更过
     */
    public List<TableColumnInfo> getStockTableColumnInfoOrder() {
        List<TableColumnInfo> fieldInfoList = getDefaultTableColumnInfo();

        if (config == null || config.getStockTableColumn() == null || config.getStockTableColumn().isEmpty()) {
            return fieldInfoList;
        }

        Map<String, TableColumnInfo> map = fieldInfoList.stream()
                .collect(Collectors.toMap(TableColumnInfo::getFieldName, Function.identity()));

        // 按配置文件排序，因为列的顺序可以变更过
        List<TableColumnInfo> result = new ArrayList<>();
        for (String fieldName : config.getStockTableColumn()) {
            TableColumnInfo fieldInfo = map.get(fieldName);
            if (fieldInfo != null) {
                result.add(fieldInfo);
                map.remove(fieldName);
            }
        }
        result.addAll(map.values());
        return result;
    }
}
