package cn.oopcoder.b2m.table.model;

import cn.oopcoder.b2m.utils.StockDataUtil;

import com.intellij.ui.table.JBTable;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Vector;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.swing.table.TableColumn;

import cn.oopcoder.b2m.bean.StockDataBean;
import cn.oopcoder.b2m.bean.ColumnDefinition;
import cn.oopcoder.b2m.config.GlobalConfigManager;

import org.jetbrains.annotations.NotNull;

import static cn.oopcoder.b2m.bean.StockDataBean.CHANGE_FIELD_NAME;
import static cn.oopcoder.b2m.bean.StockDataBean.CHANGE_PERCENT_FIELD_NAME;
import static cn.oopcoder.b2m.bean.StockDataBean.STOCK_CODE_FIELD_NAME;
import static cn.oopcoder.b2m.config.GlobalConfigManager.MOVE_FACTOR;
import static cn.oopcoder.b2m.utils.StockDataUtil.updateStockData;

/**
 * Created by oopcoder at 2025/6/2 15:32 .
 * <p>
 * 立马刷新ui
 * fireTableRowsUpdated(modelRowIndex, modelRowIndex);
 * fireTableDataChanged();
 */

public class StockTableModel extends ColumnDefinitionTableModel {

    private Map<String, StockDataBean> stockDataBeanMap;

    public StockTableModel(JBTable table) {
        super(table);
    }

    public void configStockDataBean(List<StockDataBean> stockDataBeanList) {
        this.stockDataBeanMap = stockDataBeanList.stream().
                collect(Collectors.toMap(StockDataBean::getCode, Function.identity()));
    }

    public void refresh(boolean loadData) {

        if (loadData) {
            // 加载最新数据
            updateStockData(stockDataBeanMap);
        }

        // 清空表格模型
        setRowCount(0);

        List<TableColumn> columnList = getSystemTableColumns();
        // 这里列的顺序可能变更过，恢复表头排序来取值
        columnList.sort(Comparator.comparingInt(TableColumn::getModelIndex));

        List<ColumnDefinition> fieldInfoList = columnList.stream()
                .map(tableColumn -> displayNameMap.get((String) tableColumn.getHeaderValue()))
                .collect(Collectors.toList());

        List<StockDataBean> stockDataBeans = getDefaultOrderStockDataBeanList();

        stockDataBeans.forEach(stockDataBean -> {
            Vector<Object> vector = new Vector<>(fieldInfoList.size());

            for (ColumnDefinition fieldInfo : fieldInfoList) {
                String fieldName = fieldInfo.getFieldName();
                Object fieldValue = stockDataBean.getFieldValue(fieldName);
                // 涨幅
                if (CHANGE_PERCENT_FIELD_NAME.equals(fieldName)) {
                    fieldValue = fieldValue + "%";
                }
                if (CHANGE_PERCENT_FIELD_NAME.equals(fieldName) || CHANGE_FIELD_NAME.equals(fieldName)) {
                    if (!fieldValue.toString().startsWith("-")) {
                        fieldValue = "+" + fieldValue;
                    }
                }
                vector.addElement(fieldValue);
            }
            addRow(vector);
        });
        fireTableRowsUpdated(0, table.getModel().getRowCount() - 1);
    }

    @Override
    public void setValueAt(Object aValue, int modelRowIndex, int modelColumnIndex) {
        super.setValueAt(aValue, modelRowIndex, modelColumnIndex);
        System.out.println("setValueAt(): " + aValue);

        StockDataBean stockDataBean = getStockDataBean(modelRowIndex);

        String fieldName = getColumnDefinition(modelColumnIndex).getFieldName();
        stockDataBean.setFieldValue(fieldName, aValue);

        persistStockDataBean(false);
    }

    /**
     * 获取股票编码
     *
     * @param modelRowIndex 行索引
     */
    public String getStockCode(int modelRowIndex) {
        return (String) getColumnValue(modelRowIndex, STOCK_CODE_FIELD_NAME);
    }

    /**
     * 根据编码获取 行号 modelRowIndex
     */
    public int getModelRowIndex(String code) {
        List<StockDataBean> stockDataBeans = getDefaultOrderStockDataBeanList();
        return stockDataBeans.stream().map(StockDataBean::getCode).collect(Collectors.toList()).indexOf(code);
    }

    /**
     * @return 添加后的行号 modelRowIndex，-1 表示失败
     */
    public int addStock(String code) {
        boolean codeExists = stockDataBeanMap.keySet().stream()
                .anyMatch(key -> key.equalsIgnoreCase(code));
        if (codeExists) {
            throw new RuntimeException("编码已经存在，请勿重复输入");
        }
        if (!StockDataUtil.isStockCode(code)) {
            throw new RuntimeException("编码不正确，请确认后再输入");
        }
        StockDataBean stockDataBean = new StockDataBean();
        stockDataBean.setCode(code);
        stockDataBean.setIndex(Integer.MAX_VALUE);
        stockDataBeanMap.put(code, stockDataBean);
        persistStockDataBean(true);

        return getModelRowIndex(stockDataBean.getCode());

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
        persistStockDataBean(false);
    }

    /**
     * 获取默认排序的数据
     */
    private @NotNull List<StockDataBean> getDefaultOrderStockDataBeanList() {
        return stockDataBeanMap.values().stream()
                .sorted(Comparator.comparing(StockDataBean::isPinTop)
                        .reversed()
                        .thenComparing(StockDataBean::getIndex)
                        .thenComparing(StockDataBean::getCode))
                .collect(Collectors.toList());
    }

    /**
     * @return 移动后的行号 modelRowIndex，-1 表示失败
     */
    public int moveTop(int modelRowIndex) {
        if (modelRowIndex < 0) {
            return -1;
        }

        StockDataBean stockDataBean = getStockDataBean(modelRowIndex);
        Optional<StockDataBean> optional = stockDataBeanMap.values().stream()
                .filter(s -> s.isPinTop() == stockDataBean.isPinTop())
                .min(Comparator.comparingInt(StockDataBean::getIndex).thenComparing(StockDataBean::getCode))
                .filter(s -> !s.getCode().equals(stockDataBean.getCode()));

        if (optional.isPresent()) {
            StockDataBean top = optional.get();
            stockDataBean.setIndex(top.getIndex() - MOVE_FACTOR);
            persistStockDataBean(false);
            return getModelRowIndex(stockDataBean.getCode());
        }
        return -1;
    }

    /**
     * @return 移动后的行号 modelRowIndex，-1 表示失败
     */
    public int moveBottom(int modelRowIndex) {
        if (modelRowIndex < 0) {
            return -1;
        }

        StockDataBean stockDataBean = getStockDataBean(modelRowIndex);
        Optional<StockDataBean> optional = stockDataBeanMap.values().stream()
                .filter(s -> s.isPinTop() == stockDataBean.isPinTop())
                .max(Comparator.comparingInt(StockDataBean::getIndex).thenComparing(StockDataBean::getCode))
                .filter(s -> !s.getCode().equals(stockDataBean.getCode()));

        if (optional.isPresent()) {
            StockDataBean bottom = optional.get();
            stockDataBean.setIndex(bottom.getIndex() + MOVE_FACTOR);
            persistStockDataBean(false);
            return getModelRowIndex(stockDataBean.getCode());
        }
        return -1;
    }

    /**
     * @return 移动后的行号 modelRowIndex，-1 表示失败
     */
    public int moveUp(int modelRowIndex) {
        if (modelRowIndex < 0) {
            return modelRowIndex;
        }

        StockDataBean stockDataBean = getStockDataBean(modelRowIndex);
        List<StockDataBean> list = stockDataBeanMap.values().stream()
                .filter(s -> s.isPinTop() == stockDataBean.isPinTop())
                .sorted(Comparator.comparing(StockDataBean::getIndex).thenComparing(StockDataBean::getCode))
                .collect(Collectors.toList());
        int index = list.indexOf(stockDataBean);

        if (index > 0) {
            StockDataBean preStockDataBean = list.get(index - 1);
            stockDataBean.setIndex(preStockDataBean.getIndex() - MOVE_FACTOR);
            persistStockDataBean(false);
            return modelRowIndex - 1;
        }
        return modelRowIndex;

    }

    /**
     * @return 移动后的行号 modelRowIndex，-1 表示失败
     */
    public int moveDown(int modelRowIndex) {
        if (modelRowIndex < 0) {
            return modelRowIndex;
        }
        StockDataBean stockDataBean = getStockDataBean(modelRowIndex);
        List<StockDataBean> list = stockDataBeanMap.values().stream()
                .filter(s -> s.isPinTop() == stockDataBean.isPinTop())
                .sorted(Comparator.comparing(StockDataBean::getIndex).thenComparing(StockDataBean::getCode))
                .collect(Collectors.toList());
        int index = list.indexOf(stockDataBean);

        if (index < list.size() - 1) {
            StockDataBean nextStockDataBean = list.get(index + 1);
            stockDataBean.setIndex(nextStockDataBean.getIndex() + MOVE_FACTOR);
            persistStockDataBean(false);
            return modelRowIndex + 1;
        }
        return modelRowIndex;

    }

    /**
     * 将第几行添加到固定区域
     *
     * @return 移动后的行号 modelRowIndex，-1 表示失败
     */
    public int togglePinTop(int modelRowIndex) {
        if (modelRowIndex < 0) {
            return modelRowIndex;
        }
        StockDataBean stockDataBean = getStockDataBean(modelRowIndex);
        boolean pinTop = stockDataBean.isPinTop();

        if (pinTop) {
            // 取消固定
            stockDataBeanMap.values().stream()
                    .filter(s -> !s.isPinTop())
                    .min(Comparator.comparingInt(StockDataBean::getIndex).thenComparing(StockDataBean::getCode))
                    .ifPresent(s -> stockDataBean.setIndex(s.getIndex() - 1));
        } else {
            // 固定
            stockDataBeanMap.values().stream()
                    .filter(StockDataBean::isPinTop)
                    .min(Comparator.comparingInt(StockDataBean::getIndex).thenComparing(StockDataBean::getCode))
                    .ifPresent(s -> stockDataBean.setIndex(s.getIndex() - 1));
        }
        stockDataBean.setPinTop(!pinTop);
        persistStockDataBean(false);

        return getModelRowIndex(stockDataBean.getCode());
    }

    public boolean isPinTop(int modelRowIndex) {
        if (modelRowIndex < 0) {
            return false;
        }

        StockDataBean stockDataBean = getStockDataBean(modelRowIndex);
        if (stockDataBean == null) {
            return false;
        }
        // System.out.println(modelRowIndex + " isPinTop(): " + stockDataBean.isPinTop() + " " + stockDataBean.getCode());
        return stockDataBean.isPinTop();
    }

    public StockDataBean getStockDataBean(int modelRowIndex) {
        String stockCode = getStockCode(modelRowIndex);
        return stockDataBeanMap.get(stockCode);
    }

    private void persistStockDataBean(boolean loadData) {
        List<StockDataBean> stockDataBeans = getDefaultOrderStockDataBeanList();
        GlobalConfigManager.getInstance().persistStockDataBean(stockDataBeans);
        // 修改过数据，刷新一下
        refresh(loadData);

    }
}
