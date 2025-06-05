package cn.oopcoder.b2m.table.listener;

import com.intellij.ui.table.JBTable;

import javax.swing.*;
import javax.swing.table.JTableHeader;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class ToggleRowSortMouseListener extends MouseAdapter {

    private Integer tableHeaderCount;
    private Integer tableHeaderColumnIndex;

    @Override
    public void mouseClicked(MouseEvent e) {
        JTableHeader tableHeader = (JTableHeader) e.getSource();
        // 获取点击的列索引
        int col = tableHeader.columnAtPoint(e.getPoint());
        if (col < 0) {
            return;
        }
        JBTable table = (JBTable) tableHeader.getTable();

        String colName = table.getColumnName(col);

        // 列的位置变更后，这个会对不上，需要转换, 看源码，BasicTableHeaderUI.MouseInputHandler.mouseClicked
        int columnIndex = table.convertColumnIndexToModel(col);
        System.out.printf("点击了表头，列名: 【%s】，列索引：%d -> %d\n", colName, col, columnIndex);

        // 表头默认是有排序事件的，我们要覆盖默认的点击排序事件
        RowSorter.SortKey sortKey = null;
        if (tableHeaderColumnIndex == null || tableHeaderColumnIndex != columnIndex) {
            // 第一次点击或者切换列点击
            tableHeaderColumnIndex = columnIndex;
            tableHeaderCount = 0;
            sortKey = new RowSorter.SortKey(columnIndex, SortOrder.ASCENDING);
        } else {
            tableHeaderCount++;
            int module = tableHeaderCount % 3;
            if (module == 0) {
                sortKey = new RowSorter.SortKey(columnIndex, SortOrder.ASCENDING);
            } else if (module == 1) {
                sortKey = new RowSorter.SortKey(columnIndex, SortOrder.DESCENDING);
            } else if (module == 2) {
                sortKey = new RowSorter.SortKey(columnIndex, SortOrder.UNSORTED);
            }
        }

        ArrayList<RowSorter.SortKey> keys = new ArrayList<>();
        keys.add(sortKey);
        table.getRowSorter().setSortKeys(keys);
    }

}
