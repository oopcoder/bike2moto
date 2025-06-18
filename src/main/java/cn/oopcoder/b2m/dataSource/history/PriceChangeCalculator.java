package cn.oopcoder.b2m.dataSource.history;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;

public class PriceChangeCalculator {

    private final Map<String, PriceHistory> priceHistories = new ConcurrentHashMap<>();

    @Getter
    private static final PriceChangeCalculator instance = new PriceChangeCalculator();

    private PriceChangeCalculator() {
        // 初始化定时任务，每3秒清理一次过期数据
        // ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        // scheduler.scheduleAtFixedRate(this::cleanUpOldData, 0, 3, TimeUnit.SECONDS);
    }

    /**
     * 更新价格数据, 并获取xx分钟涨幅数据
     */
    public PriceChange updatePrice(String symbol, long time, String price) {

        PriceHistory history = priceHistories.computeIfAbsent(symbol, k -> new PriceHistory());
        history.addDataPoint(symbol, time, price);

        PriceChange changes = getPriceChanges(symbol, time, price);
        return changes;
    }

    // 获取xx分钟涨幅数据
    private PriceChange getPriceChanges(String symbol, long time, String price) {
        PriceHistory history = priceHistories.get(symbol);
        if (history == null) {
            return new PriceChange("0", "0", "0");
        }

        return history.calculateChanges(time, price);
    }

    // 清理过期数据
    private void cleanUpOldData() {
        priceHistories.forEach((symbol, history) -> {
            history.cleanUp();
        });
    }
}