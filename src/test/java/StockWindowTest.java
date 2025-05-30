import cn.oopcoder.b2m.bean.StockDataBean;
import cn.oopcoder.b2m.bean.TableFieldInfo;
import cn.oopcoder.b2m.enums.ShowMode;
import cn.oopcoder.b2m.window.tool.StockWindow;
import org.junit.Test;

import java.util.List;
import java.util.Map;

public class StockWindowTest {


    @Test
    public void testGetStockData() {
        Map<String, StockDataBean> stockDataMap = StockWindow.getInitStockDataMap();
        StockWindow.updateStockData(stockDataMap);
    }

    @Test
    public void getTableColumns() {
        List<TableFieldInfo> list = StockDataBean.getTableColumns(ShowMode.Hidden);
        for (TableFieldInfo info : list) {
            System.out.println(info);
        }
    }
}
