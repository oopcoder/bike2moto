package cn.oopcoder.b2m.table.model;

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
import java.util.Vector;
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

    protected JBTable table;
    protected List<TableFieldInfo> tableFieldInfoList;
    protected Map<String, TableFieldInfo> displayNameMap;
    protected List<String> displayNameList;
    protected Map<String, TableFieldInfo> fieldNameMap;
    protected List<String> fieldNameList;

    public TableFieldInfoModel(JBTable table) {
        this.table = table;
    }

    public void setTableFieldInfo(List<TableFieldInfo> tableFieldInfoList) {
        this.tableFieldInfoList = tableFieldInfoList;
        this.displayNameMap = tableFieldInfoList.stream()
                .collect(Collectors.toMap(TableFieldInfo::displayName, Function.identity()));
        this.displayNameList = tableFieldInfoList.stream()
                .map(TableFieldInfo::displayName).collect(Collectors.toList());
        this.fieldNameMap = tableFieldInfoList.stream()
                .collect(Collectors.toMap(TableFieldInfo::fieldName, Function.identity()));
        this.fieldNameList = tableFieldInfoList.stream()
                .map(TableFieldInfo::fieldName).collect(Collectors.toList());

        setColumnIdentifiers(displayNameList.toArray());

        // 设置默认的渲染器
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int viewRowIndex, int viewColumnIndex) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, viewRowIndex,
                        viewColumnIndex);

                // 设置行背景色
                handleBackground(this, table, isSelected, viewRowIndex);
                return this;

            }

        });

        List<TableFieldInfo> colorTableFieldInfo = tableFieldInfoList.stream()
                .filter(tableFieldInfo -> !tableFieldInfo.displayColor().isEmpty())
                .toList();

        for (TableFieldInfo tableFieldInfo : colorTableFieldInfo) {

            TableColumn tableColumn = table.getColumn(tableFieldInfo.displayName());
            // 定制
            tableColumn.setCellRenderer(new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                        boolean hasFocus, int viewRowIndex, int viewColumnIndex) {

                    // 先后顺序还是有点区别，比如选中的时候这里面改了文本的颜色，但是下面自定义的
                    // 前景色把他覆盖了，所以导致选中和未选中的颜色都是一样的
                    super.getTableCellRendererComponent(table, value, isSelected, hasFocus, viewRowIndex,
                            viewColumnIndex);

                    // 设置行背景色
                    handleBackground(this, table, isSelected, viewRowIndex);

                    // 设置前景色
                    double doubleValue = NumUtil.toDouble(Objects.toString(value).replace("%", ""));
                    List<Color> colors = tableFieldInfo.displayColor();
                    if (doubleValue > 0 && colors.size() > 0) {
                        // 涨
                        Color color = colors.get(0);
                        if (color != null) {
                            setForeground(color);
                        }
                    } else if (doubleValue < 0 && colors.size() > 1) {
                        // 跌
                        Color color = colors.get(1);
                        if (color != null) {
                            setForeground(color);
                        }
                    } else if (doubleValue == 0 && colors.size() > 2) {
                        // 平
                        Color color = colors.get(2);
                        if (color != null) {
                            setForeground(color);
                        }
                    } else {
                        // 正常不会出现，除非没有配置或者bug
                        // 注意！！！这个地方是用table的颜色
                        setForeground(table.getForeground());
                    }
                    return this;
                }
            });
        }
    }

    private void handleBackground(Component component, JTable table, boolean isSelected, int viewRowIndex) {
        if (isSelected) {
            return;
        }
        // 设置行背景色
        Color backgroundColor = getBackgroundColor(table.convertRowIndexToModel(viewRowIndex));

        if (backgroundColor != null) {
            // 使用自定义颜色
            component.setBackground(backgroundColor);
            return;
        }
        // 注意！！！这个地方是用table的颜色
        component.setBackground(table.getBackground());
    }

    public Color getBackgroundColor(int modelRowIndex) {
        return null;
    }

    public String getColumnName(int modelColumnIndex) {
        return super.getColumnName(modelColumnIndex);
    }

    public TableFieldInfo getTableFieldInfo(int modelColumnIndex) {
        return tableFieldInfoList.get(modelColumnIndex);
    }

    public TableFieldInfo getTableFieldInfo(String fieldName) {
        return tableFieldInfoList.get(getColumnIndex(fieldName));
    }

    /**
     * 根据字段名找到列索引
     */
    public int getColumnIndex(String fieldName) {
        return fieldNameList.indexOf(fieldName);
    }

    public Object getColumnValue(int modelRowIndex, String fieldName) {
        int codeIndex = getColumnIndex(fieldName);
        Vector rowVector = dataVector.elementAt(modelRowIndex);
        return rowVector.elementAt(codeIndex);
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

    @Override
    public boolean isCellEditable(int row, int column) {
        TableFieldInfo fieldInfo = tableFieldInfoList.get(column);
        return fieldInfo.editable();
    }
}
