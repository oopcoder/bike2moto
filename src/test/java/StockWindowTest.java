import cn.oopcoder.b2m.bean.StockDataBean;
import cn.oopcoder.b2m.bean.ColumnDefinition;
import cn.oopcoder.b2m.enums.ShowMode;

import org.junit.Test;

import java.util.List;

public class StockWindowTest {


    @Test
    public void getTableColumns() {
        List<ColumnDefinition> list = StockDataBean.getTableColumnInfos(ShowMode.Hidden);
        for (ColumnDefinition info : list) {
            System.out.println(info);
        }
    }
}
