package cn.oopcoder.b2m.table.model;

import com.intellij.ui.table.JBTable;

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

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import cn.oopcoder.b2m.bean.ColumnDefinition;
import cn.oopcoder.b2m.utils.NumUtil;
import lombok.Getter;

/**
 * Created by oopcoder at 2025/6/2 15:32 .
 */

public class ColumnDefinitionTableModel extends DefaultTableModel {

    protected JBTable table;
    @Getter
    protected List<ColumnDefinition> columnDefinitions;
    protected Map<String, ColumnDefinition> displayNameMap;
    protected List<String> displayNameList;
    protected Map<String, ColumnDefinition> fieldNameMap;
    protected List<String> fieldNameList;

    public ColumnDefinitionTableModel(JBTable table) {
        this.table = table;
    }

    public void setColumnDefinition(List<ColumnDefinition> tableColumnInfos) {
        this.columnDefinitions = tableColumnInfos;
        this.displayNameMap = tableColumnInfos.stream()
                .collect(Collectors.toMap(ColumnDefinition::getDisplayName, Function.identity()));
        this.displayNameList = tableColumnInfos.stream()
                .map(ColumnDefinition::getDisplayName).collect(Collectors.toList());
        this.fieldNameMap = tableColumnInfos.stream()
                .collect(Collectors.toMap(ColumnDefinition::getFieldName, Function.identity()));
        this.fieldNameList = tableColumnInfos.stream()
                .map(ColumnDefinition::getFieldName).collect(Collectors.toList());

        setColumnIdentifiers(displayNameList.toArray());
    }

    public String getColumnName(int modelColumnIndex) {
        return super.getColumnName(modelColumnIndex);
    }

    public ColumnDefinition getColumnDefinition(int modelColumnIndex) {
        return columnDefinitions.get(modelColumnIndex);
    }

    public ColumnDefinition getColumnDefinition(String fieldName) {
        return columnDefinitions.get(getColumnIndex(fieldName));
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

    public List<TableColumn> getSystemTableColumns() {
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
        for (int i = 0; i < columnDefinitions.size(); i++) {
            ColumnDefinition tableFieldInfo = columnDefinitions.get(i);
            if (tableFieldInfo.isEnableNumberSorter()) {
                sorter.setComparator(i, doubleComparator);
            }
        }

        // 设置了排序器，点击表头才可以排序
        table.setRowSorter(sorter);
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        ColumnDefinition fieldInfo = columnDefinitions.get(column);
        return fieldInfo.isEditable();
    }
}
