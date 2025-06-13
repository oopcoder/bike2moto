package cn.oopcoder.b2m.dataSource;


import java.util.Map;
import java.util.Set;

public interface StockDataListener {


    public Set<String> stockCodes();

    public void updateStockData(Map<String, StockData> stockDataBeanMap);
}
