package cn.oopcoder.b2m.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import cn.oopcoder.b2m.enums.ShowMode;
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

    ShowMode showMode;

    public List<ColumnConfig> getStockColumnConfig(boolean isHiddenMode) {
        return isHiddenMode ? hiddenStockColumnConfig : normalStockColumnConfig;
    }

    public void setStockColumnConfig(boolean isHiddenMode, List<ColumnConfig> tableColumnConfig) {
        if (isHiddenMode) {
            hiddenStockColumnConfig = tableColumnConfig;
            return;
        }
        normalStockColumnConfig = tableColumnConfig;
    }
}
