package cn.oopcoder.b2m.config;

import cn.oopcoder.b2m.bean.StockDataBean;
import cn.oopcoder.b2m.utils.ReflectUtil;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class StockConfig {

    private String symbol;

    private String maskName;

    private String alias;

    // 固定在顶部
    private boolean pinTop = false;

    // 快速涨幅提醒阈值
    private Double reminderThresholdOfMin1;
    private Double reminderThresholdOfMin3;
    private Double reminderThresholdOfMin5;


    public StockConfig(StockDataBean stockDataBean) {
        ReflectUtil.copy(stockDataBean, this);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass())
            return false;
        StockConfig that = (StockConfig) o;
        return Objects.equals(symbol, that.symbol);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(symbol);
    }
}
