package cn.oopcoder.b2m.table.model;

import cn.oopcoder.b2m.config.StockConfig;

import cn.oopcoder.b2m.utils.StockDataUtil;

import com.intellij.openapi.util.Pair;
import com.intellij.ui.JBColor;
import com.intellij.ui.table.JBTable;

import java.awt.Color;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.swing.table.TableColumn;

import cn.oopcoder.b2m.bean.StockDataBean;
import cn.oopcoder.b2m.bean.TableFieldInfo;
import cn.oopcoder.b2m.config.GlobalConfigManager;

import static cn.oopcoder.b2m.bean.StockDataBean.CHANGE_PERCENT_FIELD_NAME;
import static cn.oopcoder.b2m.bean.StockDataBean.STOCK_CODE_FIELD_NAME;
import static cn.oopcoder.b2m.utils.StockDataUtil.updateStockData;

/**
 * Created by oopcoder at 2025/6/2 15:32 .
 * <p>
 * 立马刷新ui
 * fireTableRowsUpdated(modelRowIndex, modelRowIndex);
 * fireTableDataChanged();
 */

public class StockTableModel extends TableFieldInfoModel {

    private Map<String, StockDataBean> stockDataBeanMap;
    // private Map<String, Boolean> pinTopMap;

    public StockTableModel(JBTable table) {
        super(table);
    }

    public void configStockDataBeanMap(Set<StockConfig> stockConfigs) {
        this.stockDataBeanMap = stockConfigs.stream().map(StockDataBean::new).
                collect(Collectors.toMap(StockDataBean::getCode, Function.identity()));
        // this.pinTopMap = stockConfigs.stream().map(StockDataBean::new).
        //         collect(Collectors.toMap(StockDataBean::getCode, StockDataBean::isPinTop));

    }

    public void refresh() {
        // 加载最新数据
        updateStockData(stockDataBeanMap);

        // 清空表格模型
        setRowCount(0);

        List<TableColumn> columnList = getTableColumns();
        // 这里列的顺序可能变更过，恢复表头排序来取值
        columnList.sort(Comparator.comparingInt(TableColumn::getModelIndex));

        List<TableFieldInfo> fieldInfoList = columnList.stream()
                .map(tableColumn -> displayNameMap.get((String) tableColumn.getHeaderValue()))
                .toList();

        stockDataBeanMap.values().stream()
                .sorted(Comparator.comparing(StockDataBean::isPinTop).reversed()
                        .thenComparing(StockDataBean::getIndex))
                .forEach(stockDataBean -> {

                    Vector<Object> vector = new Vector<>(fieldInfoList.size());

                    for (TableFieldInfo fieldInfo : fieldInfoList) {
                        String fieldName = fieldInfo.fieldName();
                        Object fieldValue = stockDataBean.getFieldValue(fieldName);
                        // 涨幅
                        if (CHANGE_PERCENT_FIELD_NAME.equals(fieldName)) {
                            fieldValue = fieldValue + "%";
                        }
                        vector.addElement(fieldValue);
                    }
                    addRow(vector);
                });
        fireTableRowsUpdated(0, table.getModel().getRowCount() - 1);
    }

    public void setValueAt(Object aValue, int modelRowIndex, int modelColumnIndex) {
        super.setValueAt(aValue, modelRowIndex, modelColumnIndex);
        System.out.println("setValueAt(): " + aValue);

        StockDataBean stockDataBean = getStockDataBean(modelRowIndex);

        String fieldName = tableFieldInfoList.get(modelColumnIndex).fieldName();
        stockDataBean.setFieldValue(fieldName, aValue);

        persistStockConfig();
    }

    /**
     * 获取股票编码
     *
     * @param modelRowIndex 行索引
     */
    public String getStockCode(int modelRowIndex) {
        return (String) getColumnValue(modelRowIndex, STOCK_CODE_FIELD_NAME);
    }

    public void addStock(String code) {
        if (stockDataBeanMap.containsKey(code)) {
            throw new RuntimeException("编码已经存在，请勿重复输入");
        }
        if (!StockDataUtil.isStockCode(code)) {
            throw new RuntimeException("编码不正确，请确认后再输入");
        }
        StockDataBean stockDataBean = new StockDataBean();
        stockDataBean.setCode(code);
        stockDataBeanMap.put(code, stockDataBean);
        persistStockConfig();
    }

    /**
     * 删除第几行
     */
    public void remove(int modelRowIndex) {
        if (modelRowIndex < 0) {
            return;
        }
        String stockCode = getStockCode(modelRowIndex);
        removeStock(stockCode);
    }

    private void removeStock(String code) {
        stockDataBeanMap.remove(code);
        persistStockConfig();
    }

    /**
     * 将第几行添加到固定区域
     */
    public void togglePinTop(int modelRowIndex) {
        if (modelRowIndex < 0) {
            return;
        }
        StockDataBean stockDataBean = getStockDataBean(modelRowIndex);
        stockDataBean.setPinTop(!stockDataBean.isPinTop());

        // todo index 变成最小值
        persistStockConfig();
    }

    public boolean isPinTop(int modelRowIndex) {
        if (modelRowIndex < 0) {
            return false;
        }

        // String stockCode = getStockCode(modelRowIndex);
        // return pinTopMap.get(stockCode);

        StockDataBean stockDataBean = getStockDataBean(modelRowIndex);
        // System.out.println(modelRowIndex + " isPinTop(): " + stockDataBean.isPinTop() + " " + stockDataBean.getCode());
        return stockDataBean.isPinTop();
    }

    public StockDataBean getStockDataBean(int modelRowIndex) {
        String stockCode = getStockCode(modelRowIndex);
        return stockDataBeanMap.get(stockCode);
    }

    private void persistStockConfig() {
        // 持久化
        Set<StockConfig> list = stockDataBeanMap.values().stream()
                .map(t -> new StockConfig(t.getMaskName(), t.getAlias(), t.getCode(), t.getIndex(), t.isPinTop()))
                .collect(Collectors.toSet());
        GlobalConfigManager.getInstance().persistStockConfig(list);

        // 修改过数据，刷新一下
        refresh();
    }

    @Override
    public Color getBackgroundColor(int modelRowIndex) {
        return isPinTop(modelRowIndex) ? JBColor.LIGHT_GRAY : null;
    }

}
