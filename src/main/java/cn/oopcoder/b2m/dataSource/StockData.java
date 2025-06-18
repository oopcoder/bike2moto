package cn.oopcoder.b2m.dataSource;

import cn.oopcoder.b2m.utils.JacksonUtil;
import lombok.Data;

@Data
public class StockData {


    private String symbol;

    private String name;

    private String currentPrice;

    private String change;

    private String changePercent;

    private String high;

    private String low;

    private String open;

    // 昨日收盘价, 今天收盘价 就是现价
    private String preClose;

    // 振幅百分比
    private String rangePercent;

    private String time;

    @Override
    public String toString() {
        return JacksonUtil.toJson(this);
    }
}