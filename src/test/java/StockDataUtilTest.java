import cn.oopcoder.b2m.bean.StockDataBean;
import cn.oopcoder.b2m.config.GlobalConfigManager;
import cn.oopcoder.b2m.config.StockConfig;
import cn.oopcoder.b2m.utils.StockDataUtil;
import org.junit.Test;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class StockDataUtilTest {

    @Test
    public void testGetStockData() throws Exception {
        StockDataUtil.getStockData("2万");
        // StockDataUtil.getStockData("sh600900");
    }

    @Test
    public void testUpdateStockData() {
        // Set<StockConfig> stockConfigs = GlobalConfigManager.getInstance().getStockConfig();
        //
        // Map<String, StockDataBean> stockDataMap = stockConfigs.stream().map(StockDataBean::new).
        //         collect(Collectors.toMap(StockDataBean::getCode, Function.identity()));
        //
        // StockDataUtil.updateStockData(stockDataMap);
    }
}
