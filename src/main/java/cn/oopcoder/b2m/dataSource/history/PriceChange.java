package cn.oopcoder.b2m.dataSource.history;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by oopcoder at 2025/6/18 21:24 .
 */

// 涨幅结果
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PriceChange {

    public String changePercentOfMin1;
    public String changePercentOfMin3;
    public String changePercentOfMin5;
}