import cn.oopcoder.b2m.bean.StockDataBean;
import cn.oopcoder.b2m.config.GlobalConfigManager;
import cn.oopcoder.b2m.config.StockConfig;
import cn.oopcoder.b2m.dataSource.StockData;
import cn.oopcoder.b2m.utils.JacksonUtil;
import cn.oopcoder.b2m.utils.ReflectUtil;
import cn.oopcoder.b2m.utils.StockDataUtil;
import org.junit.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class StockDataUtilTest {


    List<String> list = List.of("sz300059", "sh513090", "hk01810");

    @Test
    public void testGetStockData() throws Exception {
        // String data = StockDataUtil.getStockData("2万");

        String codes = String.join(",", list);
        String data = StockDataUtil.getStockData(codes);
        System.out.println(data);
    }

    @Test
    public void testUpdateStockData() {
        Map<String, StockData> dataMap = StockDataUtil.updateStockData(new HashSet<>(list));

        for (StockData stockData : dataMap.values()) {
            System.out.println(JacksonUtil.toJson(stockData));
        }
    }


    @Test
    public void testCalculate() {
        Map<String, StockData> dataMap = StockDataUtil.updateStockData(new HashSet<>(list));

        for (StockData stockData : dataMap.values()) {
            StockDataBean stockDataBean = new StockDataBean();
            ReflectUtil.copy(stockData, stockDataBean);
            stockDataBean.calculate();

            System.out.println(JacksonUtil.toJson(stockDataBean));
        }
    }
}
