package cn.oopcoder.b2m.utils;

import cn.oopcoder.b2m.bean.StockDataBean;
import cn.oopcoder.b2m.dataSource.StockData;
import lombok.Data;
import org.apache.commons.lang3.time.DateFormatUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by oopcoder at 2025/6/2 16:07 .
 */

public class StockDataUtil {

    public static Map<String, StockData> updateStockData(Set<String> elements) {
        Map<String, StockData> dataMap = new HashMap<>();
        String codes = String.join(",", elements);
        try {
            String result = getStockData(codes);

            String[] lines = result.split("\n");
            for (String line : lines) {
                String code = line.substring(line.indexOf("_") + 1, line.indexOf("="));
                String dataStr = line.substring(line.indexOf("=") + 2, line.length() - 2);
                String[] values = dataStr.split("~");

                StockData stockData = new StockData();
                stockData.setSymbol(code);
                stockData.setName(values[1]);
                stockData.setCurrentPrice(values[3]);
                stockData.setPreClose(values[4]);
                stockData.setOpen(values[5]);
                stockData.setTime(parseTime(values[30]));
                stockData.setChange(values[31]);
                stockData.setChangePercent(values[32]);
                stockData.setHigh(values[33]);// 33
                stockData.setLow(values[34]);// 34
                stockData.setRangePercent(values[43]);

                dataMap.put(code, stockData);
                // System.out.println("parse(): " + stockDataBean);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dataMap;
    }

    private static String parseTime(String timeString) {
        try {
            Date date = new SimpleDateFormat("yyyyMMddHHmmss").parse(timeString);
            return DateFormatUtils.format(date, "HH:mm:ss");
        } catch (ParseException e) {
            try {
                Date date = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").parse(timeString);
                return DateFormatUtils.format(date, "HH:mm:ss");
            } catch (ParseException exception) {
                exception.printStackTrace();
            }
        }

        return timeString;
    }


    public static String getStockData(String codes) throws Exception {
        // 港股延时
        String result = HttpClientPool.getHttpClient().get("http://qt.gtimg.cn/q=" + codes);
        // System.out.println("http请求股票数据结果: " + result);
        return result;
    }

    /**
     * v_pv_none_match="1"; 没有匹配到
     */
    public static boolean isStockCode(String code) {
        try {
            return !"v_pv_none_match=\"1\";\n".equals(getStockData(code));
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 抛异常认为是股票代码，后续发现不是股票代码可以在界面上面做删除操作
        return true;
    }


}
