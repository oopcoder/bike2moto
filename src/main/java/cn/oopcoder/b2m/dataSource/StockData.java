package cn.oopcoder.b2m.dataSource;

import lombok.Data;

@Data
public class StockData {


    private String code;

    private String name;

    private String currentPrice;

    private String change;

    private String changePercent;

    private String high;

    private String low;

    private String time;


}