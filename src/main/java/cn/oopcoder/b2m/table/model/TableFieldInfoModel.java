package cn.oopcoder.b2m.table.model;

import com.intellij.ui.JBColor;
import com.intellij.ui.table.JBTable;

import java.awt.*;
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

import javax.swing.*;
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

        // 可以设置默认的渲染器，优先使用每列定制的渲染器
        // table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
        // });

        for (TableFieldInfo tableFieldInfo : tableFieldInfoList) {

            TableColumn tableColumn = table.getColumn(tableFieldInfo.displayName());
            // 定制
            tableColumn.setCellRenderer(new DefaultTableCellRenderer() {
                {
                    // 设置水平对齐方式为居中
                    setHorizontalAlignment(SwingConstants.CENTER);
                }

                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                               boolean hasFocus, int viewRowIndex, int viewColumnIndex) {

                    // System.out.println("\n====================" + "行：" + viewRowIndex + ", 列：" + viewColumnIndex + "====================");
                    // System.out.println(" 背景前： " + getBackground() + ", 行：" + viewRowIndex + ", 列：" + viewColumnIndex);

                    // 先后顺序还是有点区别，比如选中的时候这里面改了文本的颜色，
                    // 但是下面自定义的前景色把他覆盖了，所以导致选中和未选中的颜色都是一样的
                    Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, viewRowIndex, viewColumnIndex);
                    // System.out.println(" 背景中： " + getBackground() + ", 行：" + viewRowIndex + ", 列：" + viewColumnIndex);

                    if (!tableFieldInfo.displayColor().isEmpty()) {
                        // 设置文本颜色
                        handleForeground(component, table, value, tableFieldInfo.displayColor());
                    }

                    // 设置行背景色
                    handleBackground(component, table, isSelected, hasFocus, viewRowIndex, viewColumnIndex);
                    // System.out.println(" 背景后： " + getBackground() + ", 行：" + viewRowIndex + ", 列：" + viewColumnIndex);

                    // System.out.println(" select: " + isSelected + ", focus: " + hasFocus + ", row: " + viewRowIndex + ", column: " + viewColumnIndex);
                    if (hasFocus) {
                        // 被点击的单元格
                        // hasFocus/isSelected 不是同步的，比如代码设置选中，或者多选（Ctrl+鼠标选择），其中一行只有select，没有focus

                        // 聚焦时更改边框和大小，默认颜色 new JBColor(0x589DF6, 0x4A88C7)
                        setBorder(BorderFactory.createLineBorder(JBColor.YELLOW, 3));
                    }
                    return component;
                }
            });
        }

        // 设置表头渲染器
        table.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            {
                setHorizontalAlignment(SwingConstants.CENTER);
                setVerticalAlignment(SwingConstants.CENTER);
                // setFont(getFont().deriveFont(Font.BOLD));
            }
        });
    }

    private void handleForeground(Component component, JTable table, Object value, List<Color> colors) {
        // 设置前景色
        double doubleValue = NumUtil.toDouble(Objects.toString(value).replace("%", ""));
        if (doubleValue > 0 && !colors.isEmpty()) {
            // 涨
            component.setForeground(colors.getFirst());
            return;
        }

        if (doubleValue < 0 && colors.size() > 1) {
            // 跌
            component.setForeground(colors.get(1));
            return;
        }

        if (doubleValue == 0 && colors.size() > 2) {
            // 平
            component.setForeground(colors.get(2));
            return;
        }

        // 正常不会出现，除非没有配置或者bug
        // 注意！！！这个地方必须调用
        component.setForeground(null);
    }

    private void handleBackground(Component component, JTable table, boolean isSelected, boolean hasFocus,
                                  int viewRowIndex, int viewColumnIndex) {

        if (isSelected) {
            // 被选中的行
            // System.out.println("被选中，不处理背景色");
            return;
        }

        // 设置 null 和 不设置是有区别的，看源码注释，设置 null 是继承父类的颜色
        // 不设置的话，会延用上次渲染遗留的颜色，不是我们想要的颜色
        Color backgroundColor = getBackgroundColor(table.convertRowIndexToModel(viewRowIndex));
        component.setBackground(backgroundColor);
        // System.out.println("自定义背景颜色: " + backgroundColor + ", 行：" + viewRowIndex + ", 列：" + viewColumnIndex);
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
