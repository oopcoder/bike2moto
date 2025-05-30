package cn.oopcoder.b2m.window.tool;

import cn.oopcoder.b2m.consts.Const;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.ui.AnActionButton;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.table.JBTable;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;

public class StockWindow {
    public JPanel rootPanel;
    public JTable table;
    public volatile Integer count;
    public volatile Integer column;
    volatile boolean refreshFlag = true;

    public StockWindow() {
        createUI();
    }

    private void createUI() {
        DefaultTableModel model = new DefaultTableModel(
                new Object[][]{{5, "Alice"}, {2, "Bob"}, {3, "ijj"}, {1, "rt"}, {4, "peter"}},
                new String[]{"ID", "Name"}
        );
        table = new JBTable(model);

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        JPanel panel = ToolbarDecorator.createDecorator(table)
                .addExtraAction(new AnActionButton(Const.STOP_REFRESH_TABLE, AllIcons.Actions.Pause) {
                    @Override
                    public void actionPerformed(@NotNull AnActionEvent e) {
                        refreshFlag = !refreshFlag;
                        e.getPresentation().setIcon(refreshFlag ? AllIcons.Actions.Pause : AllIcons.Actions.Refresh);
                        e.getPresentation().setText(refreshFlag ? Const.STOP_REFRESH_TABLE : Const.CONTINUE_REFRESH_TABLE);
                        refresh(refreshFlag);
                    }

                    @Override
                    public @NotNull ActionUpdateThread getActionUpdateThread() {
                        return ActionUpdateThread.EDT;
                    }
                })
                .createPanel();

        rootPanel.add(panel, BorderLayout.CENTER);

        JTableHeader tableHeader = table.getTableHeader();
        tableHeader.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                StringBuilder tableHeadChange = new StringBuilder();
                for (int i = 0; i < table.getColumnCount(); i++) {
                    tableHeadChange.append(table.getColumnName(i)).append(",");
                }
                System.out.println("移动了列位置: " + tableHeadChange);
            }
        });

        tableHeader.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // 获取点击的列索引
                int col = tableHeader.columnAtPoint(e.getPoint());
                if (col < 0) {
                    return;
                }
                String colName = table.getColumnName(col);

                // 列的位置变更后，这个会对不上，需要转换, 看源码，BasicTableHeaderUI.MouseInputHandler.mouseClicked
                int columnIndex = table.convertColumnIndexToModel(col);
                System.out.printf("点击了表头，列名: 【%s】，列索引：%d -> %d\n", colName, col, columnIndex);

                // 表头默认是有排序事件的，我们要覆盖默认的点击排序事件
                RowSorter.SortKey sortKey = null;
                if (column == null || column != columnIndex) {
                    // 第一次点击或者切换列点击
                    column = columnIndex;
                    count = 0;
                    sortKey = new RowSorter.SortKey(columnIndex, SortOrder.ASCENDING);
                } else {
                    count++;
                    int module = count % 3;
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
        });


    }

    private void refresh(boolean refresh) {

        System.out.println("refresh(): 刷新状态: " + refresh);


    }
}
