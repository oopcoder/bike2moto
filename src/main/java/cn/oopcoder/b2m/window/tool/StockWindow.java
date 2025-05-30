package cn.oopcoder.b2m.window.tool;

import cn.oopcoder.b2m.bean.StockDataBean;
import cn.oopcoder.b2m.bean.TableFieldInfo;
import cn.oopcoder.b2m.consts.Const;
import cn.oopcoder.b2m.utils.FileUtilTest;
import cn.oopcoder.b2m.utils.HttpClientPool;
import com.fasterxml.jackson.core.type.TypeReference;
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
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

public class StockWindow {

    public JPanel rootPanel;
    private JBTable table;
    private JBLabel refreshTimeLabel;
    private JBCheckBox jbCheckBox;


    public volatile Integer count;
    public volatile Integer column;
    private volatile boolean refreshing = false;
    private DefaultTableModel tableModel;
    private Map<String, StockDataBean> stockDataBeanMap;

    public StockWindow() {
        createUI();
    }

    private void createUI() {
        tableModel = new DefaultTableModel();
        table = new JBTable(tableModel);

        TableRowSorter<TableModel> sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        ToolbarDecorator toolbarDecorator = ToolbarDecorator.createDecorator(table);
        JPanel tablePanel = toolbarDecorator.addExtraAction(new AnActionButton(Const.REFRESH_TABLE, AllIcons.Actions.Refresh) {
                    @Override
                    public void actionPerformed(@NotNull AnActionEvent e) {
                        refresh();
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
        toolbarDecorator.getActionsPanel().add(jbCheckBox, BorderLayout.WEST);


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

        // 设置表头，界面上拖动列，使列顺序变了之后，如果重新设置表头，列的顺序会按设置顺序重新排列
        tableModel.setColumnIdentifiers(tableColumns());

        stockDataBeanMap = getInitStockDataMap();

        // 第一次刷新一下
        refresh();
    }


    public String[] tableColumns() {
        return jbCheckBox.isSelected() ? StockDataBean.hiddenTableColumns : StockDataBean.normalTableColumns;
    }

    public List<TableFieldInfo> tableFieldInfo() {
        return jbCheckBox.isSelected() ? StockDataBean.hiddenTableFields : StockDataBean.normalTableFields;
    }


    public void toggleScheduledJob(boolean start) {
        System.out.println("启停定时任务: " + start);
        if (!start) {
            return;
        }
        new Thread(() -> {
            while (refreshing) {
                refresh();
                try {
                    TimeUnit.SECONDS.sleep(5);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

    public void refresh() {

        updateStockData(stockDataBeanMap);

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                refreshTimeLabel.setText(DateFormatUtils.format(new Date(), "HH:mm:ss"));
            }
        });


    }

    public static Map<String, StockDataBean> getInitStockDataMap() {
        List<StockDataBean> stockDataBeanList = FileUtilTest.fromJsonFile("config/StockConfig.json", new TypeReference<>() {
        });

        return stockDataBeanList.stream()
                .collect(Collectors.toMap(StockDataBean::getCode, Function.identity()));
    }

    public static void updateStockData(Map<String, StockDataBean> stockDataBeanMap) {

        String codes = String.join(",", stockDataBeanMap.keySet());

        try {
            String result = HttpClientPool.getHttpClient().get("http://qt.gtimg.cn/q=" + codes);

            String[] lines = result.split("\n");
            for (String line : lines) {
                String code = line.substring(line.indexOf("_") + 1, line.indexOf("="));
                String dataStr = line.substring(line.indexOf("=") + 2, line.length() - 2);
                String[] values = dataStr.split("~");

                StockDataBean stockDataBean = stockDataBeanMap.get(code);
                if (stockDataBean == null) {
                    continue;
                }

                stockDataBean.setName(values[1]);
                stockDataBean.setChange(values[31]);
                stockDataBean.setChangePercent(values[32]);
                try {
                    Date date = new SimpleDateFormat("yyyyMMddHHmmss").parse(values[30]);
                    stockDataBean.setTime(DateFormatUtils.format(date, "HH:mm:ss"));
                } catch (ParseException e) {
                    e.printStackTrace();
                    stockDataBean.setTime(values[30]);
                }

                stockDataBean.setCurrentPrice(values[3]);
                stockDataBean.setHigh(values[33]);// 33
                stockDataBean.setLow(values[34]);// 34

                System.out.println("parse(): " + stockDataBean);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
