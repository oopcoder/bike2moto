package cn.oopcoder.b2m.table;

import com.intellij.ui.table.JBTable;

import org.apache.commons.lang3.time.DateFormatUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.function.Consumer;

import javax.swing.SwingUtilities;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import cn.oopcoder.b2m.bean.StockDataBean;
import cn.oopcoder.b2m.bean.TableFieldInfo;
import cn.oopcoder.b2m.config.GlobalConfig;

import static cn.oopcoder.b2m.utils.StockDataUtil.updateStockData;

/**
 * Created by oopcoder at 2025/6/2 15:32 .
 */

public class StockTableModel extends TableFieldInfoModel {

    private Map<String, StockDataBean> stockDataBeanMap;

    public StockTableModel(JBTable table) {
        super(table);
    }

    public void setStockDataBeanMap(Map<String, StockDataBean> stockDataBeanMap) {
        this.stockDataBeanMap = stockDataBeanMap;
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
                .map(t -> columnNameMap.get((String) t.getHeaderValue())).toList();

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
                            v.addElement(stockDataBean.getFieldValue(fieldName));
                        }
                        addRow(v);
                    }
                });
        fireTableRowsUpdated(0, table.getModel().getRowCount() - 1);
    }

    public void setValueAt(Object aValue, int row, int column) {
        super.setValueAt(aValue, row, column);
        @SuppressWarnings("unchecked")
        Vector<Object> rowVector = dataVector.elementAt(row);
        int codeIndex = columnNameList.indexOf("code");
        Object o = rowVector.elementAt(codeIndex);
        System.out.println("setValueAt(): ");


        // todo 持久化
        stockDataBeanMap.get((String) o).setFieldValue(tableFieldInfoList.get(column).fieldName(), String.valueOf(aValue));
    }
}
