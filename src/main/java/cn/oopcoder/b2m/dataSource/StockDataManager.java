package cn.oopcoder.b2m.dataSource;


import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static cn.oopcoder.b2m.utils.StockDataUtil.updateStockData;

public class StockDataManager {


    private static final StockDataManager ourInstance = new StockDataManager();

    private volatile boolean refreshing = false;

    public static StockDataManager getInstance() {
        return ourInstance;
    }

    private StockDataManager() {

    }

    private Set<StockDataListener> listeners = new HashSet<>();


    public void unregister(StockDataListener stockDataListener) {
        listeners.remove(stockDataListener);
    }

    public void register(StockDataListener stockDataListener) {
        listeners.add(stockDataListener);
    }


    public void stop() {
        refreshing = false;
        System.out.println("停止定时任务");
    }

    public void start() {
        System.out.println("开始定时任务");

        // 已经启动
        if (refreshing) {
            return;
        }
        refreshing = true;

        new Thread(() -> {
            while (refreshing) {
                refresh();

                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

    public void refresh() {
        Set<String> codes = new HashSet<>();
        for (StockDataListener listener : listeners) {
            codes.addAll(listener.stockCodes());
        }
        Map<String, StockData> stockDataBeanMap = updateStockData(codes);
        for (StockDataListener listener : listeners) {
            listener.updateStockData(stockDataBeanMap);
        }

    }
}
