package cn.oopcoder.b2m.bean;


import cn.oopcoder.b2m.utils.JacksonUtil;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class StockDataBean {

    private String code;
    private String name;
    // 隐蔽模式名，最好是英文
    private String mask;
    private String alias;


    private String change; // 涨跌
    private String changePercent;

    private String time;
    private String currentPrice;
    private String high;
    private String low;

    public StockDataBean(String code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return JacksonUtil.toJson(this);
    }
}
