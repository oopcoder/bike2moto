package cn.oopcoder.b2m.window.tool;

import cn.oopcoder.b2m.bean.ColumnDefinition;
import cn.oopcoder.b2m.config.GlobalConfigManager;
import cn.oopcoder.b2m.consts.Const;
import cn.oopcoder.b2m.enums.ShowMode;
import cn.oopcoder.b2m.table.model.StockTableModel;

import cn.oopcoder.b2m.table.listener.TableColumnModelAdapter;
import cn.oopcoder.b2m.table.listener.ToggleRowSortMouseListener;

import cn.oopcoder.b2m.ui.Icons;
import cn.oopcoder.b2m.utils.NumUtil;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.ui.AnActionButton;
import com.intellij.ui.JBColor;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.table.JBTable;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;

import java.awt.*;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cn.oopcoder.b2m.consts.Const.ADD;

public class StockWindow {

    public JPanel rootPanel;
    private JBTable table;
    private JBLabel refreshTimeLabel;
    // private JBCheckBox showModeCheckBox;
    private volatile boolean refreshing = false;
    private volatile boolean selected = true;
    private StockTableModel tableModel;
    private AnActionButton addAction;
    private AnActionButton removeAction;
    private AnActionButton moveUpAction;
    private AnActionButton moveDownAction;
    private AnActionButton moveTopAction;
    private AnActionButton moveBottomAction;
    private AnActionButton pinTopAction;

    public StockWindow() {
        createUI();
        createModel();
    }

    private void createUI() {
        table = new JBTable();

        // 设置为单选模式 用户只能选中一行
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        // 禁用所有自动调整列宽的行为
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        addAction = new AnActionButton(ADD, Icons.ICON_ADD) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                // 弹窗新增逻辑
                String stockCode = JOptionPane.showInputDialog(rootPanel, "请输入新的股票代码", "新增股票", JOptionPane.PLAIN_MESSAGE);
                if (stockCode != null && !stockCode.trim().isEmpty()) {
                    try {
                        int modelRowIndex = tableModel.addStock(stockCode);
                        selectRow(modelRowIndex);
                    } catch (Exception exception) {
                        JOptionPane.showMessageDialog(rootPanel, exception.getLocalizedMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }

            @Override
            public @NotNull ActionUpdateThread getActionUpdateThread() {
                return ActionUpdateThread.EDT;
            }

        };
        removeAction = new AnActionButton(Const.REMOVE, Icons.ICON_REMOVE) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                if (table.getSelectedRow() < 0) {
                    return;
                }
                tableModel.remove(table.convertRowIndexToModel(table.getSelectedRow()));
            }

            @Override
            public @NotNull ActionUpdateThread getActionUpdateThread() {
                return ActionUpdateThread.EDT;
            }

        };

        moveUpAction = new AnActionButton(Const.MOVE_UP, Icons.ICON_MOVE_UP) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                if (table.getSelectedRow() < 0) {
                    return;
                }
                int modelRowIndex = tableModel.moveUp(table.convertRowIndexToModel(table.getSelectedRow()));
                selectRow(modelRowIndex);
            }

            @Override
            public @NotNull ActionUpdateThread getActionUpdateThread() {
                return ActionUpdateThread.EDT;
            }

        };
        moveDownAction = new AnActionButton(Const.MOVE_DOWN, Icons.ICON_MOVE_DOWN) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                if (table.getSelectedRow() < 0) {
                    return;
                }
                int modelRowIndex = tableModel.moveDown(table.convertRowIndexToModel(table.getSelectedRow()));
                selectRow(modelRowIndex);
            }

            @Override
            public @NotNull ActionUpdateThread getActionUpdateThread() {
                return ActionUpdateThread.EDT;
            }
        };
        moveTopAction = new AnActionButton(Const.MOVE_TOP, Icons.ICON_MOVE_TOP) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                if (table.getSelectedRow() < 0) {
                    return;
                }
                int modelRowIndex = tableModel.moveTop(table.convertRowIndexToModel(table.getSelectedRow()));
                selectRow(modelRowIndex);
            }

            @Override
            public @NotNull ActionUpdateThread getActionUpdateThread() {
                return ActionUpdateThread.EDT;
            }
        };
        moveBottomAction = new AnActionButton(Const.MOVE_BOTTOM, Icons.ICON_MOVE_BOTTOM) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                if (table.getSelectedRow() < 0) {
                    return;
                }
                int modelRowIndex = tableModel.moveBottom(table.convertRowIndexToModel(table.getSelectedRow()));
                selectRow(modelRowIndex);
            }

            @Override
            public @NotNull ActionUpdateThread getActionUpdateThread() {
                return ActionUpdateThread.EDT;
            }
        };
        pinTopAction = new AnActionButton(Const.PIN_TOP, Icons.ICON_PIN_TOP) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                if (table.getSelectedRow() < 0) {
                    return;
                }
                int modelRowIndex = tableModel.togglePinTop(table.convertRowIndexToModel(table.getSelectedRow()));
                selectRow(modelRowIndex);
            }

            @Override
            public @NotNull ActionUpdateThread getActionUpdateThread() {
                return ActionUpdateThread.EDT;
            }
        };
        AnAction refreshTableAction = new AnActionButton(Const.REFRESH_TABLE, AllIcons.Actions.Refresh) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                refreshModel();
            }

            @Override
            public @NotNull ActionUpdateThread getActionUpdateThread() {
                return ActionUpdateThread.EDT;
            }
        };
        AnAction continueRefreshTableAction = new AnActionButton(Const.CONTINUE_REFRESH_TABLE, AllIcons.Toolwindows.ToolWindowRun) {
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
        };
        AnAction resetDefaultConfigAction = new AnActionButton(Const.RESET_DEFAULT_CONFIG, Icons.ICON_RESET_DEFAULT_CONFIG) {
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
        };

        AnActionButton showModeAction = new AnActionButton(Const.SHOW_MODE_NORMAL, Icons.ICON_CHECKBOX_SELECTED) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                selected = !selected;
                e.getPresentation().setIcon(selected ? Icons.ICON_CHECKBOX_SELECTED : Icons.ICON_CHECKBOX_UNSELECTED);
                e.getPresentation().setText(selected ? Const.SHOW_MODE_NORMAL : Const.SHOW_MODE_HIDDEN);

                // 持久化列宽 todo 监听项目退出时持久化
                GlobalConfigManager.getInstance().persistSystemTableColumn(tableModel.getSystemTableColumns());

                beautifyTable();
                GlobalConfigManager.getInstance().setShowMode(selected ? ShowMode.Hidden : ShowMode.Normal);
                createModel();
            }

            @Override
            public @NotNull ActionUpdateThread getActionUpdateThread() {
                return ActionUpdateThread.EDT;
            }
        };
        ToolbarDecorator toolbarDecorator = ToolbarDecorator.createDecorator(table);
        JPanel tablePanel = toolbarDecorator
                .addExtraAction(showModeAction)
                .addExtraActions(refreshTableAction, continueRefreshTableAction)
                .addExtraActions(addAction, removeAction)
                .addExtraActions(moveUpAction, moveDownAction)
                .addExtraActions(moveTopAction, moveBottomAction)
                .addExtraAction(pinTopAction)
                .addExtraActions(resetDefaultConfigAction)
                .createPanel();

        rootPanel.add(tablePanel, BorderLayout.CENTER);

        refreshTimeLabel = new JBLabel();
        refreshTimeLabel.setText("请先启动定时刷新");
        refreshTimeLabel.setToolTipText("最后刷新时间");
        refreshTimeLabel.setBorder(new EmptyBorder(0, 5, 0, 5));
        toolbarDecorator.getActionsPanel().add(refreshTimeLabel, BorderLayout.EAST);

        // showModeCheckBox = new JBCheckBox();
        // showModeCheckBox.setToolTipText("隐蔽模式");
        // // 默认图标
        // showModeCheckBox.setOpaque(false); // 避免背景覆盖
        // showModeCheckBox.setBorderPainted(false); // 移除边框
        // showModeCheckBox.setIcon(Icons.ICON_CHECKBOX_UNSELECTED);
        // showModeCheckBox.setSelectedIcon(Icons.ICON_CHECKBOX_SELECTED);
        // showModeCheckBox.setTextIcon(Icons.ICON_CHECKBOX_SELECTED);
        //
        // showModeCheckBox.setSelected(true);
        // showModeCheckBox.addActionListener(e -> {
        //     beautifyTable();
        //     GlobalConfigManager.getInstance().setShowMode(showModeCheckBox.isSelected() ? ShowMode.Hidden : ShowMode.Normal);
        //     createModel();
        // });
        // toolbarDecorator.getActionsPanel().add(showModeCheckBox, BorderLayout.WEST);

        beautifyTable();

        // 列移动监听
        table.getColumnModel().addColumnModelListener(new TableColumnModelAdapter() {

            @Override
            public void columnMoved(TableColumnModelEvent e) {
                if (e.getFromIndex() == e.getToIndex()) {
                    return;
                }
                System.out.println("columnMoved(): " + e.getFromIndex() + " -> " + e.getToIndex());

                // 移动列，只会更改 TableColumn 的顺序，不会修改我们通过 setColumnIdentifiers 设置的表头
                GlobalConfigManager.getInstance().persistSystemTableColumn(tableModel.getSystemTableColumns());
            }
        });

        JTableHeader tableHeader = table.getTableHeader();
        tableHeader.addMouseListener(new ToggleRowSortMouseListener());

        // 添加编辑器监听
        table.getDefaultEditor(Object.class)
                .addCellEditorListener(new CellEditorListener() {
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

        // 获取行选择模型
        ListSelectionModel selectionModel = table.getSelectionModel();

        // 添加行选择监听器
        selectionModel.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {  // 避免多次触发
                    enabledActionOnSelectRow();
                }
            }
        });

        enabledActionOnSelectRow();
    }

    private void enabledActionOnSelectRow() {
        int selectedRow = table.getSelectedRow();
        System.out.println("selectedRow: " + selectedRow);

        boolean isSelect = selectedRow != -1;

        removeAction.setEnabled(isSelect);
        moveUpAction.setEnabled(isSelect);
        moveDownAction.setEnabled(isSelect);
        moveTopAction.setEnabled(isSelect);
        moveBottomAction.setEnabled(isSelect);
        pinTopAction.setEnabled(isSelect);
    }

    private void selectRow(int modelRowIndex) {
        if (modelRowIndex >= 0) {
            int viewRowIndex = table.convertRowIndexToView(modelRowIndex);
            table.setRowSelectionInterval(viewRowIndex, viewRowIndex); // 选中单行
        }

    }

    private void beautifyTable() {
        // 不显示网格线
        // table.setShowGrid(!jbCheckBox.isSelected());
        // 设置表格条纹（斑马线） darcula 主题才有显示，其他主题可能不明显，看不出来
        // table.setStriped(!jbCheckBox.isSelected());
    }

    private void createModel() {

        tableModel = new StockTableModel(table);
        table.setModel(tableModel);

        // 设置表头
        tableModel.setColumnDefinition(GlobalConfigManager.getInstance().getStockColumnDefinition());

        // 配置排序器
        tableModel.configRowSorter();

        // 配置要监控的股票
        tableModel.configStockDataBean(GlobalConfigManager.getInstance().getStockDataBean());

        tableModel.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                if (e.getColumn() < 0) {
                    return;
                }
                StockTableModel tableModel = (StockTableModel) e.getSource();
                ColumnDefinition fieldInfo = tableModel.getColumnDefinition(e.getColumn());
                if (fieldInfo == null) {
                    return;
                }
                if (e.getType() == TableModelEvent.UPDATE) {
                    if (fieldInfo.isEditable()) {
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

        // 配置渲染器
        configRenderer();

        // 第一次刷新一下
        refreshModel();
    }

    public void configRenderer() {

        // todo 会导致默认的排序箭头不见了，暂时移除

        // 设置表头渲染器
        // table.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
        //     {
        //         setHorizontalAlignment(SwingConstants.CENTER);
        //         setVerticalAlignment(SwingConstants.CENTER);
        //         setFont(getFont().deriveFont(Font.BOLD));
        //     }
        // });

        // 可以设置默认的渲染器，优先使用每列定制的渲染器
        // table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
        // });

        // boolean selected = showModeCheckBox.isSelected();

        List<ColumnDefinition> columnDefinitions = tableModel.getColumnDefinitions();
        Map<String, ColumnDefinition> tableColumnInfoMap = columnDefinitions.stream()
                .collect(Collectors.toMap(ColumnDefinition::getDisplayName, Function.identity()));

        List<TableColumn> systemTableColumns = tableModel.getSystemTableColumns();
        for (TableColumn tableColumn : systemTableColumns) {
            String displayName = (String) tableColumn.getHeaderValue();
            ColumnDefinition columnDefinition = tableColumnInfoMap.get(displayName);

            Integer preferredWidth = columnDefinition.getPreferredWidth();
            if (preferredWidth != null) {
                // 设置默认的列宽，源码不建议使用setWidth()方法
                tableColumn.setPreferredWidth(preferredWidth);
            }

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

                    if (!columnDefinition.getDisplayColor().isEmpty()) {
                        // 设置文本颜色
                        handleForeground(component, table, value, columnDefinition.getDisplayColor());
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
    }

    private void handleForeground(Component component, JTable table, Object value, List<Color> colors) {
        // 设置前景色
        double doubleValue = NumUtil.toDouble(Objects.toString(value).replace("%", ""));
        if (doubleValue > 0 && !colors.isEmpty()) {
            // 涨
            component.setForeground(colors.get(0));
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
        Color backgroundColor = tableModel.isPinTop(table.convertRowIndexToModel(viewRowIndex)) ? JBColor.LIGHT_GRAY : null;
        component.setBackground(backgroundColor);
        // System.out.println("自定义背景颜色: " + backgroundColor + ", 行：" + viewRowIndex + ", 列：" + viewColumnIndex);
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
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

    public void refreshModel() {
        tableModel.refresh(true);
        SwingUtilities.invokeLater(() -> refreshTimeLabel.setText(DateFormatUtils.format(new Date(), "HH:mm:ss")));
    }

}
