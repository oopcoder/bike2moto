package cn.oopcoder.b2m.table;

import cn.oopcoder.b2m.config.StockConfig;

import com.intellij.ui.table.JBTable;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.function.Consumer;
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
 */

public class StockTableModel extends TableFieldInfoModel {

    private Map<String, StockDataBean> stockDataBeanMap;

    public StockTableModel(JBTable table) {
        super(table);
    }

    public void configStockDataBeanMap(Set<StockConfig> stockConfigs) {
        this.stockDataBeanMap = stockConfigs.stream().map(StockDataBean::new).
                collect(Collectors.toMap(StockDataBean::getCode, Function.identity()));
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
                .map(t -> displayNameMap.get((String) t.getHeaderValue())).toList();

        stockDataBeanMap.values().stream()
                .sorted(Comparator.comparing(StockDataBean::getIndex, new Comparator<Integer>() {
                    @Override
                    public int compare(Integer o1, Integer o2) {
                        return o2.compareTo(o1);
                    }
                }))// 默认排序
                .forEach(new Consumer<StockDataBean>() {
                    @Override
                    public void accept(StockDataBean stockDataBean) {
                        Vector<Object> v = new Vector<>(fieldInfoList.size());

                        for (TableFieldInfo fieldInfo : fieldInfoList) {
                            String fieldName = fieldInfo.fieldName();
                            Object fieldValue = stockDataBean.getFieldValue(fieldName);
                            // 涨幅
                            if (CHANGE_PERCENT_FIELD_NAME.equals(fieldName)) {
                                fieldValue = fieldValue + "%";
                            }
                            v.addElement(fieldValue);
                        }
                        addRow(v);
                    }
                });
        fireTableRowsUpdated(0, table.getModel().getRowCount() - 1);
    }

    public void setValueAt(Object aValue, int row, int column) {
        super.setValueAt(aValue, row, column);
        // 根据code字段名找到code的列索引
        int codeIndex = getColumnIndex(STOCK_CODE_FIELD_NAME);
        Vector<Object> rowVector = dataVector.elementAt(row);

        Object code = rowVector.elementAt(codeIndex);
        System.out.println("setValueAt(): ");

        StockDataBean stockDataBean = stockDataBeanMap.get((String) code);
        String fieldName = tableFieldInfoList.get(column).fieldName();
        stockDataBean.setFieldValue(fieldName, aValue);

        persistStockConfig();
    }

    public void addStock(String code) {
        if (stockDataBeanMap.containsKey(code)) {
            throw new RuntimeException("编码已经存在，请勿重复输入");
        }
        StockDataBean stockDataBean = new StockDataBean();
        stockDataBean.setCode(code);
        stockDataBeanMap.put(code, stockDataBean);
        persistStockConfig();
    }


    private void persistStockConfig() {
        // 持久化
        Set<StockConfig> list = stockDataBeanMap.values().stream()
                .map(t -> new StockConfig(t.getMaskName(), t.getAlias(), t.getCode(), t.getIndex()))
                .collect(Collectors.toSet());
        GlobalConfigManager.getInstance().persistStockConfig(list);
    }
}
