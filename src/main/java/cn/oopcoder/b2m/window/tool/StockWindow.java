package cn.oopcoder.b2m.window.tool;

import cn.oopcoder.b2m.bean.TableFieldInfo;
import cn.oopcoder.b2m.config.GlobalConfigManager;
import cn.oopcoder.b2m.consts.Const;
import cn.oopcoder.b2m.enums.ShowMode;
import cn.oopcoder.b2m.table.StockTableModel;

import cn.oopcoder.b2m.table.TableColumnModelAdapter;
import cn.oopcoder.b2m.table.ToggleRowSortMouseListener;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.ui.AnActionButton;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.table.JBTable;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.JTableHeader;

import java.awt.*;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class StockWindow {

    public JPanel rootPanel;
    private JBTable table;
    private JBLabel refreshTimeLabel;
    private JBCheckBox jbCheckBox;
    private volatile boolean refreshing = false;
    private StockTableModel tableModel;

    public StockWindow() {
        createUI();

        createModel();
    }

    private void createUI() {
        table = new JBTable();

        ToolbarDecorator toolbarDecorator = ToolbarDecorator.createDecorator(table);
        JPanel tablePanel = toolbarDecorator
                .setAddActionName("新增").setAddAction(anAction -> { // 添加按钮回调
                    // 弹窗新增逻辑
                    String stockCode = JOptionPane.showInputDialog(rootPanel, "请输入新的股票代码", "新增股票", JOptionPane.PLAIN_MESSAGE);
                    if (stockCode != null && !stockCode.trim().isEmpty()) {
                        try {
                            tableModel.addStock(stockCode);
                            refreshModel();
                        } catch (Exception e) {
                            JOptionPane.showMessageDialog(rootPanel, e.getLocalizedMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                })

                .setRemoveActionName("删除").setRemoveAction(anAction -> { // 删除按钮回调
                    tableModel.remove(table.getSelectedRow());
                    refreshModel();
                })

                .setEditActionName("固定").setEditAction(anAction -> {
                    // 暂时用这个按钮实现
                    tableModel.pinTop(table.getSelectedRow());
                    refreshModel();
                })

                .setMoveUpActionName("上移").setMoveUpAction(anAction -> { /* 上移逻辑 */ })
                .setMoveDownActionName("上移").setMoveDownAction(anAction -> { /* 下移逻辑 */ })
                .addExtraAction(new AnActionButton(Const.REFRESH_TABLE, AllIcons.Actions.Refresh) {
                    @Override
                    public void actionPerformed(@NotNull AnActionEvent e) {
                        refreshModel();
                    }

                    @Override
                    public @NotNull ActionUpdateThread getActionUpdateThread() {
                        return ActionUpdateThread.EDT;
                    }
                })
                .addExtraAction(new AnActionButton(Const.CONTINUE_REFRESH_TABLE, AllIcons.Toolwindows.ToolWindowRun) {
                    @Override
                    public void actionPerformed(@NotNull AnActionEvent e) {
                        refreshing = !refreshing;
                        e.getPresentation().setIcon(refreshing ? AllIcons.Actions.Pause : AllIcons.Toolwindows.ToolWindowRun);
                        e.getPresentation().setText(refreshing ? Const.STOP_REFRESH_TABLE : Const.CONTINUE_REFRESH_TABLE);

                        toggleScheduledJob(refreshing);
                    }

                    @Override
                    public @NotNull ActionUpdateThread getActionUpdateThread() {
                        return ActionUpdateThread.EDT;
                    }
                })
                .addExtraAction(new AnActionButton(Const.RESET_CONFIG, AllIcons.General.Reset) {
                    @Override
                    public void actionPerformed(@NotNull AnActionEvent e) {
                        int result = JOptionPane.showConfirmDialog(
                                rootPanel,
                                "确定要恢复默认配置吗？此操作不可撤销！",
                                "确认恢复",
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.WARNING_MESSAGE
                        );
                        if (result == JOptionPane.YES_OPTION) {
                            GlobalConfigManager.getInstance().clear();
                            createModel();
                        }
                    }

                    @Override
                    public @NotNull ActionUpdateThread getActionUpdateThread() {
                        return ActionUpdateThread.EDT;
                    }
                })
                .createPanel();

        rootPanel.add(tablePanel, BorderLayout.CENTER);

        refreshTimeLabel = new JBLabel();
        refreshTimeLabel.setText("请先启动定时刷新");
        refreshTimeLabel.setToolTipText("最后刷新时间");
        refreshTimeLabel.setBorder(new EmptyBorder(0, 5, 0, 5));
        toolbarDecorator.getActionsPanel().add(refreshTimeLabel, BorderLayout.EAST);

        jbCheckBox = new JBCheckBox();
        jbCheckBox.setToolTipText("隐蔽模式");
        jbCheckBox.setSelected(true);
        jbCheckBox.addActionListener(e -> {
            GlobalConfigManager.getInstance().setShowMode(jbCheckBox.isSelected() ? ShowMode.Hidden : ShowMode.Normal);
            createModel();
        });

        toolbarDecorator.getActionsPanel().add(jbCheckBox, BorderLayout.WEST);

        table.getColumnModel().addColumnModelListener(new TableColumnModelAdapter() {

            @Override
            public void columnMoved(TableColumnModelEvent e) {
                if (e.getFromIndex() == e.getToIndex()) {
                    return;
                }
                System.out.println("columnMoved(): " + e.getFromIndex() + " -> " + e.getToIndex());

                // 移动列，只会更改 TableColumn 的顺序，不会修改我们通过 setColumnIdentifiers 设置的表头

                List<String> displayNames = tableModel.getTableColumns().stream()
                        .map(tableColumn -> (String) tableColumn.getHeaderValue())
                        .collect(Collectors.toList());
                GlobalConfigManager.getInstance().persistStockTableColumn(displayNames);
            }
        });

        JTableHeader tableHeader = table.getTableHeader();
        tableHeader.addMouseListener(new ToggleRowSortMouseListener());

        // 添加编辑器监听
        table.getDefaultEditor(Object.class).addCellEditorListener(new CellEditorListener() {
            @Override
            public void editingStopped(ChangeEvent e) {
                // 可以在这里获取编辑后的值
                System.out.println("单元格 编辑器监听");
            }

            @Override
            public void editingCanceled(ChangeEvent e) {
                // 编辑取消处理
            }
        });

    }

    private void createModel() {

        tableModel = new StockTableModel(table);
        table.setModel(tableModel);

        // 设置表头，界面上拖动列，使列顺序变了之后，如果重新设置表头，列的顺序会按设置顺序重新排列
        tableModel.setTableFieldInfo(GlobalConfigManager.getInstance().getStockTableFieldInfoOrder());

        // 配置排序器
        tableModel.configRowSorter();

        // 配置要监控的股票
        tableModel.configStockDataBeanMap(GlobalConfigManager.getInstance().getStockConfig());

        tableModel.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                if (e.getColumn() < 0) {
                    return;
                }
                StockTableModel tableModel = (StockTableModel) e.getSource();
                TableFieldInfo fieldInfo = tableModel.getTableFieldInfo(e.getColumn());
                if (fieldInfo == null) {
                    return;
                }
                if (e.getType() == TableModelEvent.UPDATE) {
                    if (fieldInfo.editable()) {
                        int row = e.getFirstRow();
                        int column = e.getColumn();
                        // todo 找到 该行的 code，最该map对应的值
                        System.out.println("单元格修改: 行 " + row + ", 列 " + column);
                    }
                } else if (e.getType() == TableModelEvent.INSERT) {
                    // 行添加
                } else if (e.getType() == TableModelEvent.DELETE) {
                    // 行删除
                }

                // if (e.getColumn() == COLUMN_INDEX_TO_MONITOR) {
                //     // 特定列修改处理
                // }
            }
        });

        // 第一次刷新一下
        refreshModel();
    }

    public void toggleScheduledJob(boolean start) {
        System.out.println("启停定时任务: " + start);
        if (!start) {
            return;
        }
        new Thread(() -> {
            while (refreshing) {
                refreshModel();
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

    public void refreshModel() {
        tableModel.refresh();
        SwingUtilities.invokeLater(() -> refreshTimeLabel.setText(DateFormatUtils.format(new Date(), "HH:mm:ss")));
    }

}
