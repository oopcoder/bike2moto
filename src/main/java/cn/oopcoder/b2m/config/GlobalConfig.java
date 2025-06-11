package cn.oopcoder.b2m.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GlobalConfig {

    List<ColumnConfig> normalStockColumnConfig;

    List<ColumnConfig> hiddenStockColumnConfig;

    List<StockConfig> stockConfig;

    public List<ColumnConfig> getStockColumnConfig(boolean isHidden) {
        return isHidden ? hiddenStockColumnConfig : normalStockColumnConfig;
    }

    public void setStockColumnConfig(boolean isHidden, List<ColumnConfig> tableColumnConfig) {
        if (isHidden) {
            hiddenStockColumnConfig = tableColumnConfig;
            return;
        }
        normalStockColumnConfig = tableColumnConfig;
    }
}
