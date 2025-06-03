import cn.oopcoder.b2m.bean.StockDataBean;
import cn.oopcoder.b2m.bean.TableFieldInfo;
import cn.oopcoder.b2m.config.GlobalConfigManager;
import cn.oopcoder.b2m.config.StockConfig;
import cn.oopcoder.b2m.enums.ShowMode;
import cn.oopcoder.b2m.utils.StockDataUtil;

import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class StockWindowTest {

    @Test
    public void testGetStockData() {
        List<StockConfig> stockConfigs = GlobalConfigManager.getInstance().getStockConfig();

        Map<String, StockDataBean> stockDataMap = stockConfigs.stream().map(StockDataBean::new).
                collect(Collectors.toMap(StockDataBean::getCode, Function.identity()));

        StockDataUtil.updateStockData(stockDataMap);
    }

    @Test
    public void getTableColumns() {
        List<TableFieldInfo> list = StockDataBean.getTableColumns(ShowMode.Hidden);
        for (TableFieldInfo info : list) {
            System.out.println(info);
        }
    }
}
