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

import javax.swing.table.TableColumn;

import cn.oopcoder.b2m.bean.StockDataBean;
import cn.oopcoder.b2m.bean.ColumnDefinition;
import cn.oopcoder.b2m.enums.ShowMode;
import cn.oopcoder.b2m.utils.FileUtil;
import cn.oopcoder.b2m.utils.JacksonUtil;

import static cn.oopcoder.b2m.enums.ShowMode.Hidden;
import static cn.oopcoder.b2m.enums.ShowMode.Normal;

/**
 * Created by oopcoder at 2025/5/31 9:42 .
 */

public class GlobalConfigManager {

    private static final GlobalConfigManager ourInstance = new GlobalConfigManager();

    private static final String GLOBAL_CONFIG_KEY = "b2m.global.config";

    public static List<ColumnDefinition> hiddenTableColumnInfos;
    public static List<ColumnDefinition> normalTableColumnInfos;

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
        hiddenTableColumnInfos = null;
        normalTableColumnInfos = null;
    }

    // 排序间隔，必须要大于 MOVE_FACTOR，
    // 不然在置顶、置底、上移、下移逻辑时会出现 index 相等的情况，从而导致一些奇怪的问题，
    // 比如想要上移1行，却上移了3行，因为 index 一样的时候，根据 code 排序，所以出现了移动多行的现象
    public static final int ORDER_FACTOR = 10;
    public static final int MOVE_FACTOR = 1;

    public List<StockDataBean> getStockDataBean() {
        List<StockConfig> stockConfigs = getStockConfig();
        int index = 1;
        List<StockDataBean> stockDataBeanList = new ArrayList<>();
        for (StockConfig stockConfig : stockConfigs) {
            stockDataBeanList.add(new StockDataBean(stockConfig, ORDER_FACTOR * index++));
        }
        return stockDataBeanList;
    }

    public void persistStockDataBean(List<StockDataBean> stockDataBeans) {
        int index = 1;
        for (StockDataBean stockDataBean : stockDataBeans) {
            stockDataBean.setIndex(ORDER_FACTOR * index++);
        }

        // 按顺序 持久化
        List<StockConfig> list = stockDataBeans.stream()
                .map(t -> new StockConfig(t.getCode(), t.getMaskName(), t.getAlias(), t.isPinTop()))
                .collect(Collectors.toList());

        persistStockConfig(list);
    }

    private void persistStockConfig(List<StockConfig> stockConfig) {
        if (config == null) {
            config = new GlobalConfig();
        }
        config.setStockConfig(stockConfig);
        persist();
    }

    private List<StockConfig> getStockConfig() {
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

    private void persistStockColumnConfig(boolean isHiddenMode, List<ColumnConfig> tableColumnConfigs) {
        if (config == null) {
            config = new GlobalConfig();
        }
        config.setStockColumnConfig(isHiddenMode, tableColumnConfigs);
        persist();
    }

    private List<ColumnConfig> getStockColumnConfig(boolean isHiddenMode) {

        if (config == null || config.getStockColumnConfig(isHiddenMode) == null) {
            // 第一次加载
            List<ColumnDefinition> defaultTableColumnInfo = getDefaultTableColumnInfo(isHiddenMode);
            List<ColumnConfig> tableColumnConfigs = new ArrayList<>();
            for (ColumnDefinition definition : defaultTableColumnInfo) {
                ColumnConfig columnConfig = new ColumnConfig();
                columnConfig.setFieldName(definition.getFieldName());
                columnConfig.setPreferredWidth(definition.getPreferredWidth());
                tableColumnConfigs.add(columnConfig);
            }
            persistStockColumnConfig(isHiddenMode, tableColumnConfigs);
            return tableColumnConfigs;
        }

        return config.getStockColumnConfig(isHiddenMode);
    }

    private List<ColumnDefinition> getDefaultTableColumnInfo(boolean isHiddenMode) {
        if (isHiddenMode) {
            if (hiddenTableColumnInfos == null) {
                hiddenTableColumnInfos = StockDataBean.getTableColumnInfos(Hidden);
            }
            return hiddenTableColumnInfos;
        }
        if (normalTableColumnInfos == null) {
            normalTableColumnInfos = StockDataBean.getTableColumnInfos(Normal);
        }
        return normalTableColumnInfos;
    }

    /**
     * 隐藏模式 是隐藏名，正常模式 是正常中文名
     */
    public void persistSystemTableColumn(boolean isHiddenMode, List<TableColumn> systemTableColumns) {

        List<ColumnDefinition> defaultTableColumnInfo = getDefaultTableColumnInfo(isHiddenMode);
        Map<String, ColumnDefinition> tableColumnInfoMap = defaultTableColumnInfo.stream()
                .collect(Collectors.toMap(ColumnDefinition::getDisplayName, Function.identity()));

        List<ColumnConfig> stockTableColumnConfig = getStockColumnConfig(isHiddenMode);
        Map<String, ColumnConfig> tableColumnConfigMap = stockTableColumnConfig.stream()
                .collect(Collectors.toMap(ColumnConfig::getFieldName, Function.identity()));

        List<ColumnConfig> orderList = new ArrayList<>();
        // 重新排序
        for (TableColumn tableColumn : systemTableColumns) {
            String displayName = (String) tableColumn.getHeaderValue();
            ColumnDefinition columnDefinition = tableColumnInfoMap.get(displayName);

            ColumnConfig columnConfig = tableColumnConfigMap.get(columnDefinition.getFieldName());
            if (columnConfig != null) {
                columnConfig.setPreferredWidth(tableColumn.getPreferredWidth());
            } else {
                // 新增的列还没配置
                columnConfig = new ColumnConfig();
                columnConfig.setFieldName(columnDefinition.getFieldName());
                columnConfig.setPreferredWidth(columnDefinition.getPreferredWidth());
            }

            orderList.add(columnConfig);
        }
        persistStockColumnConfig(isHiddenMode, orderList);
    }

    /**
     * 按配置文件排序，因为列的顺序可以变更过
     */
    public List<ColumnDefinition> getStockColumnDefinition(boolean isHiddenMode) {

        Map<String, ColumnDefinition> tableColumnInfoMap = getDefaultTableColumnInfo(isHiddenMode).stream()
                .collect(Collectors.toMap(ColumnDefinition::getFieldName, Function.identity()));

        List<ColumnDefinition> orderList = new ArrayList<>();

        // 按配置文件排序，因为列的顺序可能变更过
        int index = 1;
        List<ColumnConfig> columnConfigs = getStockColumnConfig(isHiddenMode);
        for (ColumnConfig columnConfig : columnConfigs) {
            ColumnDefinition columnDefinition = tableColumnInfoMap.remove(columnConfig.getFieldName());
            if (columnDefinition != null) {
                // 列减少的时候会为null
                columnDefinition.setPreferredWidth(columnConfig.getPreferredWidth());
                columnDefinition.setOrder(index++);
                orderList.add(columnDefinition);
            }
        }

        // 列新增的时候
        for (Map.Entry<String, ColumnDefinition> entry : tableColumnInfoMap.entrySet()) {
            ColumnDefinition columnDefinition = entry.getValue();
            columnDefinition.setOrder(index++);
            orderList.add(columnDefinition);
        }
        return orderList;
    }

    public ShowMode getShowMode() {
        if (config == null || config.getShowMode() == null) {
            persistShowMode(Normal);
        }
        return config.getShowMode();
    }

    public void persistShowMode(ShowMode showMode) {
        if (config == null) {
            config = new GlobalConfig();
            config.setShowMode(showMode);
            persist();
            return;
        }
        config.setShowMode(showMode);
        persist();
    }

}
