package cn.oopcoder.b2m.table;

import com.intellij.ui.JBColor;
import com.intellij.ui.table.JBTable;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import cn.oopcoder.b2m.bean.TableFieldInfo;
import cn.oopcoder.b2m.utils.NumUtil;

/**
 * Created by oopcoder at 2025/6/2 15:32 .
 */

public class TableFieldInfoModel extends DefaultTableModel {

    JBTable table;
    List<TableFieldInfo> tableFieldInfoList;
    Map<String, TableFieldInfo> columnNameMap;
    List<String> columnNameList;

    public TableFieldInfoModel(JBTable table) {
        this.table = table;
    }

    public void setTableFieldInfo(List<TableFieldInfo> tableFieldInfoList) {
        this.tableFieldInfoList = tableFieldInfoList;
        this.columnNameMap = tableFieldInfoList.stream()
                .collect(Collectors.toMap(TableFieldInfo::displayName, Function.identity()));
        this.columnNameList = tableFieldInfoList.stream().map(TableFieldInfo::displayName).collect(Collectors.toList());
        setColumnIdentifiers(columnNameList.toArray());

        List<TableFieldInfo> colorTableFieldInfo = tableFieldInfoList.stream()
                .filter(tableFieldInfo -> !tableFieldInfo.displayColor().isEmpty()).toList();

        for (TableFieldInfo tableFieldInfo : colorTableFieldInfo) {

            TableColumn tableColumn = table.getColumn(tableFieldInfo.displayName());
            tableColumn.setCellRenderer(new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                        boolean hasFocus, int row, int column) {

                    double doubleValue = NumUtil.toDouble(Objects.toString(value).replace("%", ""));

                    List<Color> colors = tableFieldInfo.displayColor();
                    if (doubleValue > 0 && colors.size() > 0) {
                        // 涨
                        Color color = colors.get(0);
                        if (color != null) {
                            setForeground(JBColor.RED);
                        }
                    } else if (doubleValue < 0 && colors.size() > 1) {
                        // 跌
                        Color color = colors.get(1);
                        if (color != null) {
                            setForeground(JBColor.GREEN);
                        }
                    } else if (doubleValue == 0 && colors.size() > 2) {
                        // 平
                        Color color = colors.get(2);
                        if (color != null) {
                            setForeground(color);
                        }
                    } else {
                        // 正常不会出现，除非没有配置或者bug
                        setForeground(getForeground());
                    }
                    return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                }
            });
        }
    }

    public String getColumnName(int columnIndex) {
        return super.getColumnName(columnIndex);
    }

    public TableFieldInfo getTableFieldInfo(int columnIndex) {
        return tableFieldInfoList.get(columnIndex);
    }

    public List<TableColumn> getTableColumns() {
        TableColumnModel columnModel = table.getColumnModel();
        Enumeration<TableColumn> columns = columnModel.getColumns();
        Iterator<TableColumn> iterator = columns.asIterator();

        List<TableColumn> columnList = new ArrayList<>();
        while (iterator.hasNext()) {
            TableColumn column = iterator.next();
            columnList.add(column);
        }
        return columnList;
    }

    public void configRowSorter() {
        TableRowSorter<TableModel> sorter = new TableRowSorter<>(this);

        Comparator<Object> doubleComparator = (o1, o2) -> {
            Double v1 = NumUtil.toDouble(Objects.toString(o1).replace("%", ""));
            Double v2 = NumUtil.toDouble(Objects.toString(o2).replace("%", ""));
            return v1.compareTo(v2);
        };

        // 有些字符串字段 要转成 数字进行排序
        for (int i = 0; i < tableFieldInfoList.size(); i++) {
            TableFieldInfo tableFieldInfo = tableFieldInfoList.get(i);
            if (tableFieldInfo.enableNumberSorter()) {
                sorter.setComparator(i, doubleComparator);
            }
        }

        // 设置了排序器，点击表头才可以排序
        table.setRowSorter(sorter);
    }
}
