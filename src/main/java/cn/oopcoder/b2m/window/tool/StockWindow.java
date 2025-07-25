package cn.oopcoder.b2m.window.tool;

import cn.oopcoder.b2m.bean.ColumnDefinition;
import cn.oopcoder.b2m.bean.StockDataBean;
import cn.oopcoder.b2m.config.GlobalConfigManager;
import cn.oopcoder.b2m.consts.Const;
import cn.oopcoder.b2m.dataSource.StockDataManager;
import cn.oopcoder.b2m.factory.ProjectHolder;
import cn.oopcoder.b2m.table.model.StockTableModel;

import cn.oopcoder.b2m.table.listener.TableColumnModelAdapter;
import cn.oopcoder.b2m.table.listener.ToggleRowSortMouseListener;

import cn.oopcoder.b2m.ui.Icons;
import cn.oopcoder.b2m.utils.NumUtil;

import cn.oopcoder.b2m.utils.ReflectUtil;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.ui.AnActionButton;
import com.intellij.ui.JBColor;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.table.JBTable;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.plaf.basic.BasicCheckBoxUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cn.oopcoder.b2m.bean.StockDataBean.Min1_FIELD_NAME;
import static cn.oopcoder.b2m.bean.StockDataBean.Min3_FIELD_NAME;
import static cn.oopcoder.b2m.bean.StockDataBean.Min5_FIELD_NAME;
import static cn.oopcoder.b2m.bean.StockDataBean.Threshold1_FIELD_NAME;
import static cn.oopcoder.b2m.bean.StockDataBean.Threshold3_FIELD_NAME;
import static cn.oopcoder.b2m.bean.StockDataBean.Threshold5_FIELD_NAME;
import static cn.oopcoder.b2m.consts.Const.ADD;
import static cn.oopcoder.b2m.enums.ShowMode.Hidden;
import static cn.oopcoder.b2m.enums.ShowMode.Normal;

public class StockWindow {

    public JPanel rootPanel;
    private JBTable table;
    private JBLabel lastUpdateTimeLabel;
    private JCheckBox refreshCheckBox;
    private volatile boolean selectedHidden;
    private StockTableModel tableModel;
    private AnActionButton addAction;
    private AnActionButton removeAction;
    private AnActionButton moveUpAction;
    private AnActionButton moveDownAction;
    private AnActionButton moveTopAction;
    private AnActionButton moveBottomAction;
    private AnActionButton pinTopAction;
    private ToolbarDecorator toolbarDecorator;
    private JPanel tablePanel;

    public StockWindow() {
        createUI();
        createModel();
    }

    private void createUI() {
        table = new JBTable();

        // 设置为单选模式 用户只能选中一行
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        // 禁用所有自动调整列宽的行为，不禁用，下面没有滚动条，列宽随着窗口大小变化自动压缩变化
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        addAction = new AnActionButton(ADD, Icons.ICON_ADD) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                // String stockCode = JOptionPane.showInputDialog(rootPanel, "请输入新的股票代码", "新增股票", JOptionPane.PLAIN_MESSAGE);
                // 弹窗新增逻辑
                JPanel panel = new JPanel(new BorderLayout(0, 10));
                panel.setBorder(new EmptyBorder(10, 10, 10, 10));
                
                JLabel inputLabel = new JLabel("请输入新的股票代码");
                inputLabel.setFont(inputLabel.getFont().deriveFont(Font.BOLD));
                
                JTextField textField = new JTextField();
                panel.add(inputLabel, BorderLayout.NORTH);
                panel.add(textField, BorderLayout.CENTER);

                int result = JOptionPane.showConfirmDialog(
                        rootPanel,
                        panel,
                        "新增股票", 
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.PLAIN_MESSAGE
                );
                if (result != JOptionPane.OK_OPTION) {
                    return;
                }
                String stockCode = textField.getText().trim();
                if (!stockCode.isEmpty()) {
                    try {
                        int modelRowIndex = tableModel.addStock(stockCode);
                        selectRow(modelRowIndex);
                    } catch (Exception exception) {
                        // 创建带样式的错误提示面板
                        JPanel errorPanel = new JPanel(new BorderLayout(0, 10));
                        errorPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
                        
                        JLabel errorLabel = new JLabel(exception.getLocalizedMessage());
                        errorLabel.setFont(errorLabel.getFont().deriveFont(Font.BOLD));
                        
                        errorPanel.add(errorLabel, BorderLayout.CENTER);
                        
                        JOptionPane.showMessageDialog(
                            rootPanel,
                            errorPanel,
                            "错误",
                            JOptionPane.ERROR_MESSAGE
                        );
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
                StockDataManager.getInstance().refresh();
            }

            @Override
            public @NotNull ActionUpdateThread getActionUpdateThread() {
                return ActionUpdateThread.EDT;
            }
        };
        AnAction resetDefaultConfigAction = new AnActionButton(Const.RESET_DEFAULT_CONFIG, Icons.ICON_RESET_DEFAULT_CONFIG) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                JPanel panel = new JPanel(new BorderLayout(0, 10));
                panel.setBorder(new EmptyBorder(10, 10, 10, 10));
                
                JLabel warningLabel = new JLabel("确定要恢复默认配置吗？此操作不可撤销！");
                warningLabel.setFont(warningLabel.getFont().deriveFont(Font.BOLD));
                
                JCheckBox clearStockCheckBox = new JCheckBox("同时清除股票配置", false);
                clearStockCheckBox.setBorder(new EmptyBorder(5, 0, 0, 0));
                
                panel.add(warningLabel, BorderLayout.NORTH);
                panel.add(clearStockCheckBox, BorderLayout.CENTER);

                int result = JOptionPane.showConfirmDialog(
                        rootPanel,
                        panel,
                        "确认恢复",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );
                if (result != JOptionPane.YES_OPTION) {
                    return;
                }
                GlobalConfigManager.getInstance().clear(clearStockCheckBox.isSelected());
                createModel();

                // 更新其他窗口的图标
                List<ProjectHolder> projectHolders = ProjectHolder.getProjectHolderExclude(StockWindow.this);
                for (ProjectHolder projectHolder : projectHolders) {
                    System.out.println("项目即将更新: " + projectHolder.getProject().getName());
                    projectHolder.getStockWindow().createModel();
                }
            }

            @Override
            public @NotNull ActionUpdateThread getActionUpdateThread() {
                return ActionUpdateThread.EDT;
            }
        };

        selectedHidden = GlobalConfigManager.getInstance().getShowMode() == Hidden;

        AnActionButton showModeAction = new AnActionButton(selectedHidden ? Const.SHOW_MODE_NORMAL : Const.SHOW_MODE_HIDDEN,
                selectedHidden ? Icons.ICON_CHECKBOX_SELECTED : Icons.ICON_CHECKBOX_UNSELECTED) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {

                selectedHidden = !selectedHidden;
                e.getPresentation().setIcon(selectedHidden ? Icons.ICON_CHECKBOX_SELECTED : Icons.ICON_CHECKBOX_UNSELECTED);
                e.getPresentation().setText(selectedHidden ? Const.SHOW_MODE_NORMAL : Const.SHOW_MODE_HIDDEN);

                GlobalConfigManager.getInstance().persistShowMode(selectedHidden ? Hidden : Normal);

                createModel();

                // InputEvent inputEvent = e.getInputEvent();
                // ActionButton source = (ActionButton) inputEvent.getSource();
                // ActionToolbarImpl parent = (ActionToolbarImpl) source.getParent();
                // System.out.println("actionPerformed(): ");
            }

            @Override
            public @NotNull ActionUpdateThread getActionUpdateThread() {
                return ActionUpdateThread.EDT;
            }
        };
        toolbarDecorator = ToolbarDecorator.createDecorator(table);
        tablePanel = toolbarDecorator
                .addExtraActions(refreshTableAction)
                .addExtraActions(addAction, removeAction)
                .addExtraActions(moveUpAction, moveDownAction)
                .addExtraActions(moveTopAction, moveBottomAction)
                .addExtraAction(pinTopAction)
                .addExtraActions(showModeAction, resetDefaultConfigAction)
                .createPanel();

        rootPanel.add(tablePanel, BorderLayout.CENTER);

        lastUpdateTimeLabel = new JBLabel();
        lastUpdateTimeLabel.setToolTipText("最后刷新时间");
        lastUpdateTimeLabel.setBorder(new EmptyBorder(0, 5, 0, 5));
        toolbarDecorator.getActionsPanel().add(lastUpdateTimeLabel, BorderLayout.EAST);

        // 在ToolbarDecorator放不了，因为没找到从外部更新图标的方法
        refreshCheckBox = new JBCheckBox();
        refreshCheckBox.setToolTipText(Const.CONTINUE_REFRESH_TABLE);
        // 强制使用自定义 UI，不然图标就是无法更改
        refreshCheckBox.setUI(new BasicCheckBoxUI());
        refreshCheckBox.setIcon(AllIcons.Toolwindows.ToolWindowRun);
        refreshCheckBox.setSelectedIcon(AllIcons.Actions.Pause);
        toolbarDecorator.getActionsPanel().add(refreshCheckBox, BorderLayout.WEST);
        refreshCheckBox.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                // #2B2D30
                refreshCheckBox.setBackground(JBColor.decode("#3D3F42"));
                // refreshCheckBox.setOpaque(true);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                refreshCheckBox.setBackground(null);
                // refreshCheckBox.setOpaque(false);
            }
        });

        ProjectHolder existProject = ProjectHolder.getFirstProjectHolderExclude(this);
        if (existProject != null) {
            refreshCheckBox.setSelected(existProject.getStockWindow().refreshCheckBox.isSelected());
        }

        refreshCheckBox.addActionListener(e -> {
            JBCheckBox source = (JBCheckBox) e.getSource();

            source.setToolTipText(source.isSelected() ? Const.STOP_REFRESH_TABLE : Const.CONTINUE_REFRESH_TABLE);
            toggleScheduledJob(source.isSelected());
            tableModel.enableCellEdit(!source.isSelected());

            // 更新其他窗口的图标
            List<ProjectHolder> projectHolders = ProjectHolder.getProjectHolderExclude(this);
            for (ProjectHolder projectHolder : projectHolders) {
                System.out.println("项目即将更新: " + projectHolder.getProject().getName());
                projectHolder.getStockWindow().updateRefreshCheckBox(source.isSelected());
            }
        });

        // 列移动监听
        table.getColumnModel().addColumnModelListener(new TableColumnModelAdapter() {

            // 列位置改变
            @Override
            public void columnMoved(TableColumnModelEvent e) {
                if (e.getFromIndex() == e.getToIndex()) {
                    return;
                }
                System.out.println("columnMoved(): " + e.getFromIndex() + " -> " + e.getToIndex());

                // 移动列，只会更改 TableColumn 的顺序，不会修改我们通过 setColumnIdentifiers 设置的表头
                GlobalConfigManager.getInstance().persistSystemTableColumn(selectedHidden,
                        tableModel.getSystemTableColumns());
            }

            // 列宽度改变
            @Override
            public void columnMarginChanged(ChangeEvent e) {
                TableColumnModel model = table.getColumnModel();
                for (int i = 0; i < model.getColumnCount(); i++) {
                    System.out.printf("Column %d (%s) width: %d%n",
                            i, model.getColumn(i).getHeaderValue(), model.getColumn(i).getWidth());
                }

                GlobalConfigManager.getInstance().persistSystemTableColumn(selectedHidden,
                        tableModel.getSystemTableColumns());
            }
        });

        JTableHeader tableHeader = table.getTableHeader();
        tableHeader.addMouseListener(new ToggleRowSortMouseListener());

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

        // 不显示网格线
        // table.setShowGrid(!jbCheckBox.isSelected());
        // 设置表格条纹（斑马线） darcula 主题才有显示，其他主题可能不明显，看不出来
        // table.setStriped(!jbCheckBox.isSelected());
    }

    public void toggleScheduledJob(boolean start) {
        if (start) {
            StockDataManager.getInstance().start();
            return;
        }
        StockDataManager.getInstance().stop();
    }

    private void enabledActionOnSelectRow() {
        int selectedRow = table.getSelectedRow();
        // System.out.println("selectedRow: " + selectedRow);

        boolean isSelect = selectedRow != -1;

        removeAction.setEnabled(isSelect);
        moveUpAction.setEnabled(isSelect);
        moveDownAction.setEnabled(isSelect);
        moveTopAction.setEnabled(isSelect);
        moveBottomAction.setEnabled(isSelect);
        pinTopAction.setEnabled(isSelect);
    }

    public void selectRow(int modelRowIndex) {
        if (modelRowIndex >= 0) {
            int viewRowIndex = table.convertRowIndexToView(modelRowIndex);
            table.setRowSelectionInterval(viewRowIndex, viewRowIndex); // 选中单行
        }
    }

    public void createModel() {
        if (tableModel != null) {
            StockDataManager.getInstance().unregister(tableModel);
        }
        tableModel = new StockTableModel(this, table);
        tableModel.enableCellEdit(!refreshCheckBox.isSelected());
        StockDataManager.getInstance().register(tableModel);

        table.setModel(tableModel);

        // 设置表头
        tableModel.setColumnDefinition(GlobalConfigManager.getInstance().getStockColumnDefinition(selectedHidden));

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
        StockDataManager.getInstance().refresh();
    }

    public void configRenderer() {

        // 设置表头渲染器 保持默认的排序图标行为
        TableCellRenderer defaultRenderer = table.getTableHeader().getDefaultRenderer();
        table.getTableHeader().setDefaultRenderer(new TableCellRenderer() {

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component delegate = defaultRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (delegate instanceof JLabel) {
                    JLabel cmp = (JLabel) delegate;
                    cmp.setHorizontalAlignment(SwingConstants.CENTER);
                    cmp.setVerticalAlignment(SwingConstants.CENTER);
                    cmp.setFont(cmp.getFont().deriveFont(Font.BOLD));
                    return cmp;
                } else {
                    return delegate;
                }
            }
        });

        // 优先使用每列定制的渲染器，没定制则用默认的渲染器
        // 也可以覆盖默认的渲染器， 看JTable.createDefaultRenderers()方法，是根据类型来选择用哪个渲染器
        // table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
        // });

        // 添加编辑器监听
        // table.getDefaultEditor(Object.class)
        //      .addCellEditorListener(new CellEditorListener() {
        //          @Override
        //          public void editingStopped(ChangeEvent e) {
        //              // 可以在这里获取编辑后的值
        //              System.out.println("单元格 编辑器监听");
        //          }
        //
        //          @Override
        //          public void editingCanceled(ChangeEvent e) {
        //              // 编辑取消处理
        //          }
        //      });

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

            // TableCellRenderer cellRenderer = tableColumn.getCellRenderer();
            // if (cellRenderer == null) {
            //     cellRenderer = table.getCellRenderer(0, tableColumn.getModelIndex());
            // }

            tableColumn.setCellRenderer(new DefaultTableCellRenderer.UIResource() {

                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                               boolean hasFocus, int viewRowIndex, int viewColumnIndex) {

                    // System.out.println("\n====================" + "行：" + viewRowIndex + ", 列：" + viewColumnIndex + "====================");
                    // System.out.println(" 背景前： " + getBackground() + ", 行：" + viewRowIndex + ", 列：" + viewColumnIndex);

                    // 先后顺序还是有点区别，比如选中的时候这里面改了文本的颜色，
                    // 但是下面自定义的前景色把他覆盖了，所以导致选中和未选中的颜色都是一样的
                    Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
                            viewRowIndex, viewColumnIndex);
                    // System.out.println(" 背景中： " + getBackground() + ", 行：" + viewRowIndex + ", 列：" + viewColumnIndex);

                    StockDataBean stockDataBean = tableModel.getStockDataBean(table.convertRowIndexToModel(viewRowIndex));

                    // 设置文本颜色
                    handleForeground(component, table, value, columnDefinition, viewRowIndex, viewColumnIndex, stockDataBean);

                    // 设置行背景色
                    handleBackground(component, table, isSelected, hasFocus, viewRowIndex, viewColumnIndex, stockDataBean);
                    // System.out.println(" 背景后： " + getBackground() + ", 行：" + viewRowIndex + ", 列：" + viewColumnIndex);

                    // System.out.println(" select: " + isSelected + ", focus: " + hasFocus + ", row: " + viewRowIndex + ", column: " + viewColumnIndex);
                    if (hasFocus) {
                        // 被点击的单元格
                        // hasFocus/isSelected 不是同步的，比如代码设置选中，或者多选（Ctrl+鼠标选择），其中一行只有select，没有focus

                        // 聚焦时更改边框和大小，默认颜色 new JBColor(0x589DF6, 0x4A88C7)
                        setBorder(BorderFactory.createLineBorder(JBColor.YELLOW, 3));
                    }
                    setHorizontalAlignment(SwingConstants.CENTER);
                    setVerticalAlignment(SwingConstants.CENTER);
                    return component;
                }
            });
        }
    }

    private void handleForeground(Component component, JTable table, Object value, ColumnDefinition columnDefinition,
                                  int viewRowIndex, int viewColumnIndex, StockDataBean stockDataBean) {
        List<Color> colors = columnDefinition.getDisplayColor();
        if (colors.isEmpty()) {
            return;
        }
        double colorThreshold = getColorThreshold(columnDefinition, stockDataBean);
        // 设置前景色
        double doubleValue = NumUtil.toDouble(Objects.toString(value).replace("%", ""));
        if (doubleValue > colorThreshold) {
            // 涨
            component.setForeground(colors.get(0));
            return;
        }

        if (doubleValue < -colorThreshold && colors.size() > 1) {
            // 跌
            component.setForeground(colors.get(1));
            return;
        }

        if (doubleValue >= -colorThreshold && doubleValue <= colorThreshold && colors.size() > 2) {
            // 平
            component.setForeground(colors.get(2));
            return;
        }

        // 正常不会出现，除非没有配置或者bug
        // 注意！！！这个地方必须调用
        component.setForeground(null);
    }

    private static double getColorThreshold(ColumnDefinition columnDefinition, StockDataBean stockDataBean) {

        Object fieldValue = null;
        if (Min1_FIELD_NAME.equals(columnDefinition.getFieldName())) {
            fieldValue = ReflectUtil.getFieldValue(Threshold1_FIELD_NAME, stockDataBean);
        }
        if (Min3_FIELD_NAME.equals(columnDefinition.getFieldName())) {
            fieldValue = ReflectUtil.getFieldValue(Threshold3_FIELD_NAME, stockDataBean);
        }
        if (Min5_FIELD_NAME.equals(columnDefinition.getFieldName())) {
            fieldValue = ReflectUtil.getFieldValue(Threshold5_FIELD_NAME, stockDataBean);
        }

        double colorThreshold;
        if (ObjectUtils.isNotEmpty(fieldValue)) {
            colorThreshold = NumUtil.toDouble(fieldValue );
        } else {
            colorThreshold = columnDefinition.getColorThreshold();
        }

        return colorThreshold;
    }

    private void handleBackground(Component component, JTable table, boolean isSelected, boolean hasFocus,
                                  int viewRowIndex, int viewColumnIndex, StockDataBean stockDataBean) {
        if (isSelected || stockDataBean == null) {
            // 被选中的行
            // System.out.println("被选中，不处理背景色");
            return;
        }

        // 设置 null 和 不设置是有区别的，看源码注释，设置 null 是继承父类的颜色
        // 不设置的话，会延用上次渲染遗留的颜色，不是我们想要的颜色
        Color backgroundColor = stockDataBean.isPinTop() ? JBColor.LIGHT_GRAY : null;
        component.setBackground(backgroundColor);
        // System.out.println("自定义背景颜色: " + backgroundColor + ", 行：" + viewRowIndex + ", 列：" + viewColumnIndex);
    }

    public void refreshTimeLabel() {
        SwingUtilities.invokeLater(() -> lastUpdateTimeLabel.setText(DateFormatUtils.format(new Date(), "HH:mm:ss")));
    }

    /**
     * 项目关闭时，资源回收
     */
    public void projectClosing(Project project) {

        StockDataManager.getInstance().unregister(tableModel);
    }

    // 外部更新
    public void updateRefreshCheckBox(boolean selected) {
        // 代码设置的，监听器不会回调
        refreshCheckBox.setSelected(selected);
        refreshCheckBox.setToolTipText(selected ? Const.STOP_REFRESH_TABLE : Const.CONTINUE_REFRESH_TABLE);
        tableModel.enableCellEdit(!selected);
    }
}
