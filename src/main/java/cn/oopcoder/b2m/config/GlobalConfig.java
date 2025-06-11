package cn.oopcoder.b2m.config;

import cn.oopcoder.b2m.enums.ShowMode;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import static cn.oopcoder.b2m.enums.ShowMode.Hidden;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GlobalConfig {

    List<TableColumnConfig> normalStockTableColumnConfig;

    List<TableColumnConfig> hiddenStockTableColumnConfig;

    List<StockConfig> stockConfig;


    public List<TableColumnConfig> getStockTableColumnConfig(boolean isHidden) {
        return isHidden ? hiddenStockTableColumnConfig : normalStockTableColumnConfig;
    }

    public void setStockTableColumnConfig(boolean isHidden, List<TableColumnConfig> tableColumnConfig) {
        if (isHidden) {
            hiddenStockTableColumnConfig = tableColumnConfig;
            return;
        }
        normalStockTableColumnConfig = tableColumnConfig;
    }
}
