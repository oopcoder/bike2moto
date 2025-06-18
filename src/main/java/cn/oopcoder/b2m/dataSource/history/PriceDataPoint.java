package cn.oopcoder.b2m.dataSource.history;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Created by oopcoder at 2025/6/18 21:24 .
 * 价格数据点
 */
@Data
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PriceDataPoint {

    String symbol;
    long time;
    String price;
}