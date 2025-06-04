package cn.oopcoder.b2m.utils;

import org.apache.commons.lang3.time.DateFormatUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import cn.oopcoder.b2m.bean.StockDataBean;

/**
 * Created by oopcoder at 2025/6/2 16:07 .
 */

public class StockDataUtil {

    public static void updateStockData(Map<String, StockDataBean> stockDataBeanMap) {
        String codes = String.join(",", stockDataBeanMap.keySet());
        try {
            String result = getStockData(codes);

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


    public static String getStockData(String codes) throws Exception {
        String result = HttpClientPool.getHttpClient().get("http://qt.gtimg.cn/q=" + codes);
        System.out.println("http请求股票数据结果: " + result);
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
