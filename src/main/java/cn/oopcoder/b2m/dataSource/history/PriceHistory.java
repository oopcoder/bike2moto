package cn.oopcoder.b2m.dataSource.history;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import static cn.oopcoder.b2m.consts.Const.EMPTY_VALUE;

/**
 * Created by oopcoder at 2025/6/18 21:21 .
 * 价格历史数据类
 */

public class PriceHistory {

    // 最大保存6分钟的数据 (360秒)，每3秒一个数据点，大约120个数据点
    private static final int CLEAN_MINUTES_AGO = 6 * 60 * 1000;
    private static final int MAX_DATA_POINTS = CLEAN_MINUTES_AGO / 300 + 5;

    // 使用LinkedHashMap保持插入顺序，便于清理最老的数据
    private final LinkedHashMap<Long, PriceDataPoint> dataPoints = new LinkedHashMap<>();

    public synchronized void addDataPoint(String symbol, long time, String price) {
        dataPoints.put(time, new PriceDataPoint(symbol, time, price));

        // 如果数据点超过最大数量，移除最老的一个
        if (dataPoints.size() > MAX_DATA_POINTS) {
            dataPoints.remove(dataPoints.keySet().iterator().next());
        }
    }

    public synchronized void cleanUp() {
        try {
            String time = new SimpleDateFormat("HH:mm:ss").format(new Date());
            Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("1970-01-01 " + time);
            long cutoffTime = date.getTime() - CLEAN_MINUTES_AGO;
            dataPoints.entrySet().removeIf(entry -> entry.getKey() < cutoffTime);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized PriceChange calculateChanges(long time, String price) {
        if (dataPoints.isEmpty()) {
            return new PriceChange(EMPTY_VALUE, EMPTY_VALUE, EMPTY_VALUE);
        }

        long oneMinuteAgo = time - (60 * 1000);
        long threeMinutesAgo = time - (3 * 60 * 1000);
        long fiveMinutesAgo = time - (5 * 60 * 1000);

        try {
            double currentPrice = Double.parseDouble(price);

            // 获取各时间点的价格，如果找不到则返回null
            Double oneMinPrice = findPriceAtTime(oneMinuteAgo);
            Double threeMinPrice = findPriceAtTime(threeMinutesAgo);
            Double fiveMinPrice = findPriceAtTime(fiveMinutesAgo);

            // 计算涨幅
            String oneMinChange = (oneMinPrice != null) ?
                    calculateChangePercent(currentPrice, oneMinPrice) : EMPTY_VALUE;
            String threeMinChange = (threeMinPrice != null) ?
                    calculateChangePercent(currentPrice, threeMinPrice) : EMPTY_VALUE;
            String fiveMinChange = (fiveMinPrice != null) ?
                    calculateChangePercent(currentPrice, fiveMinPrice) : EMPTY_VALUE;
            return new PriceChange(oneMinChange, threeMinChange, fiveMinChange);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return new PriceChange(EMPTY_VALUE, EMPTY_VALUE, EMPTY_VALUE);
        }
    }

    private Double findPriceAtTime(long targetTime) {
        // 允许的时间误差范围
        final long MAX_TIME_DIFF = 6000;

        // 遍历所有数据点，寻找在目标时间±3秒内的数据点
        for (Map.Entry<Long, PriceDataPoint> entry : dataPoints.entrySet()) {
            // System.out.println("findPriceAtTime(): " + entry.getKey());
            long diff = Math.abs(entry.getKey() - targetTime);
            if (diff <= MAX_TIME_DIFF) {
                try {
                    return Double.parseDouble(entry.getValue().price);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        }

        // 如果没有找到在误差范围内的数据点，返回null
        return null;
    }

    private String calculateChangePercent(double currentPrice, double oldPrice) {
        if (oldPrice == 0) {
            return EMPTY_VALUE;
        }

        double change = ((currentPrice - oldPrice) / oldPrice) * 100;
        return String.format("%.2f", change);
    }
}

